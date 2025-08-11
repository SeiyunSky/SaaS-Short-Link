package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import molu.common.convention.exception.ClientException;
import molu.common.convention.exception.ServiceException;
import molu.common.enums.ValidDateTypeEnum;
import molu.config.GotoDomainWhiteListConfiguration;
import molu.dao.entity.*;
import molu.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import molu.dto.biz.ShortLinkStatsRecordDTO;
import molu.dto.req.ShortLinkBatchCreateReqDTO;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.req.ShortLinkPageReqDTO;
import molu.dto.req.ShortLinkUpdateReqDTO;
import molu.dto.resp.*;
import molu.mq.producer.DelayShortLinkStatsProducer;
import molu.toolkit.HashUtil;
import molu.toolkit.LinkUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import molu.service.ShortLinkService;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static molu.common.constant.RedisKeyConstant.*;
import static molu.common.constant.ShortUrlConstant.AMAP_REMOTE_URL;


/**
 * 短链接实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkGotoMapper shortLinkGoToMapper;
    private final RBloomFilter<String> linkCreateRegisterCachePenetrationBloomFilter;
    private final ShortLinkMapper shortLinkMapper;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    @Value("${short-link.stats.locale.amap-key}")
    private String localeMapKey;

    @Value("${short-link.domain.default}")
    private String defaultDomain;

    /**
     * 创建短链接
     * @param requestParam 创建
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        if((requestParam.getValidDateType()!=0 && Objects.equals(requestParam.getValidDate(),null))
        ||(!Objects.equals(requestParam.getValidDate(),null)) && requestParam.getValidDate().before(new Date())){
            throw new ClientException("自定义过期时间小于当前时间了！");
        }
        //可跳转白名单管理
        //verificationWhitelist(requestParam.getOriginUrl());
        //创建短链接
        String ret = generateSuffix(requestParam);
        String fullshortLink = StrBuilder
                .create(defaultDomain)
                .append("/")
                .append(ret)
                .toString();
        ShortLinkDO shortLinkDO =ShortLinkDO.builder()
                .domain(defaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .describe(requestParam.getDescribe())
                .shortUri(ret)
                .totalPv(0)
                .totalPv(0)
                .totalUip(0)
                .delTime(0L)
                .enableStatus(0)
                .fullShortUrl(fullshortLink)
                .build();

        //创建路由
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullshortLink)
                .gid(requestParam.getGid())
                .build();
        //解决布隆杠过滤器误判问题，在数据库内复查
        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGoToMapper.insert(linkGotoDO);
        }catch (DuplicateKeyException exception){
            //在数据库内进行查询
            LambdaQueryWrapper<ShortLinkDO> queryWrapper =  Wrappers.lambdaQuery(ShortLinkDO.class)
                            .eq(ShortLinkDO::getFullShortUrl, shortLinkDO.getFullShortUrl());
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if(hasShortLinkDO!=null){
                log.warn("短链接: {} 重复入库",requestParam.getDomain()+"/"+ret);
                throw new ServiceException("短链接生成重复");
            }
        }
        //创建期间直接塞进Redis,进行缓存预热
        stringRedisTemplate.opsForValue()
                .set(String.format(GOTO_KEY,fullshortLink), requestParam.getOriginUrl(), LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()),TimeUnit.MILLISECONDS);
        //将完整URL添加进布隆过滤器内
        linkCreateRegisterCachePenetrationBloomFilter.add(StrBuilder
                .create(requestParam.getDomain())
                .append("/")
                .append(ret)
                .toString());

        //返回DTO
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(shortLinkDO.getGid())
                .build();
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }


    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each->BeanUtil.toBean(each,ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkCountQueryRespDTO> groupShortLinkCount(List<String> requestParam) {
        return shortLinkMapper.groupShortLinkCount(requestParam);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ShortLinkUpdateReqDTO requestParam) {

        //可跳转白名单管理
        //verificationWhitelist(requestParam.getOriginUrl());

        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getDelTime,0L)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = shortLinkMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLinkDO.getOriginUrl()) ? hasShortLinkDO.getFavicon() : getFavicon(requestParam.getOriginUrl()))
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            //拿到读写锁
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            rLock.lock();
            try {
                //来试试看查询对应的数据，并且完成删除
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0)
                        .set(ShortLinkDO::getDelFlag,1)
                        .set(ShortLinkDO::getDelTime, System.currentTimeMillis());
                int updatedRows = baseMapper.update(null, linkUpdateWrapper);
                log.info("Updated {} rows", updatedRows);
                //真正更新数据库内容在这里
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(defaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDO.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDO.getShortUri())
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLinkDO.getOriginUrl()) ? hasShortLinkDO.getFavicon() : getFavicon(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                shortLinkMapper.insert(shortLinkDO);

                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGoToMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGoToMapper.delete(linkGotoQueryWrapper);
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGoToMapper.insert(shortLinkGotoDO);
            } finally {
                rLock.unlock();
            }
        }
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate())
                || !Objects.equals(hasShortLinkDO.getOriginUrl(), requestParam.getOriginUrl())) {
            stringRedisTemplate.delete(String.format(GOTO_KEY, requestParam.getFullShortUrl()));
            Date currentDate = new Date();
            if (hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().before(currentDate)) {
                if (Objects.equals(requestParam.getValidDateType(),ValidDateTypeEnum.PERMANENT.getType()) || requestParam.getValidDate().after(currentDate)) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
        }


    /**
     * 核心功能，跳转
     * @param shortUri 短链接后缀
     * @param request 请求
     * @param response 响应
     */
    @Transactional
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        String fullShortUrl = request.getServerName()+":"+request.getServerPort()+"/"+shortUri;

        //在Redis缓存里查询数据
        String originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_KEY,fullShortUrl));

        if(StrUtil.isNotBlank(originLink)){
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
            response.sendRedirect(originLink);
            return;
        }

        //对于缓存穿透，第一层，布隆过滤器
        boolean contains = linkCreateRegisterCachePenetrationBloomFilter.contains(fullShortUrl);
        if(!contains){
            response.sendRedirect("/page/notfound");
            return;
        }
        //检查是不是空数据，避免对无效空数据发起查询
        String gotoIsNull = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_KEY,fullShortUrl));
        if(StrUtil.isNotBlank(gotoIsNull)){
            response.sendRedirect("/page/notfound");
            return;
        }

        // 布隆过滤器内存在的话，加分布式锁，防止缓存击穿
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_KEY,fullShortUrl));
        lock.lock();

        try {
            // 双检锁：再次检查缓存，因为阻塞的进程在首个完成查找更新缓存后，剩下的进程不用再去数据库找
            originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_KEY,fullShortUrl));
            if(StrUtil.isNotBlank(originLink)){
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
                response.sendRedirect(originLink);
                return;
            }

            // 从数据库查询短链接路由信息
            LambdaQueryWrapper<ShortLinkGotoDO> wrapper =Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGoToMapper.selectOne(wrapper);
            //如果数据库不存在路由信息，报错
            if(shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_KEY,fullShortUrl),"-",30, TimeUnit.MINUTES);
                response.sendRedirect("/page/notfound");
                return;
            }

            // 查询有效的短链接详情
            LambdaQueryWrapper<ShortLinkDO> ret = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(ret);
            // 如果没找到有效短链接，就放弃寻找
            if(shortLinkDO == null || (shortLinkDO.getValidDate().before(new Date()) && shortLinkDO.getValidDate()!=null)) {
                //如果过期，就是无了
                 stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_KEY,fullShortUrl),"-",30, TimeUnit.MINUTES);
                    response.sendRedirect("/page/notfound");
                    return;

            }
            stringRedisTemplate.opsForValue()
                    .set(String.format(GOTO_KEY,fullShortUrl), shortLinkDO.getOriginUrl(),
                            LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),TimeUnit.MILLISECONDS
                    );
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
            response.sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock(); // 释放锁
        }
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl,HttpServletRequest request, HttpServletResponse response){

        //绕过 Java Lambda 表达式对外部局部变量“事实最终”的限制
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        //获取请求内的所有cookie
        Cookie[] cookies = request.getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    @Override
    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if (!rLock.tryLock()) {
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
        try {
            if(StrUtil.isBlank(gid)){
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGoToMapper.selectOne(Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl,fullShortUrl)
                );
                gid = shortLinkGotoDO.getGid();
            }
            //获取指定小时
            int hour = DateUtil.hour(new Date(), true);
            //获取指定星期
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int value = week.getIso8601Value();

            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(value)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(DateUtil.beginOfDay(new Date()))
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO) ;

            //根据IP地址解析地区
            Map<String,Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key",localeMapKey);
            localeParamMap.put("ip",statsRecord.getRemoteAddr());
            String localeRet = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            System.out.println("API返回结果：" + localeRet);
            System.out.println("Key: " + localeMapKey);
            System.out.println("IP: " + statsRecord.getRemoteAddr());
            System.out.println("请求URL: " + AMAP_REMOTE_URL + "?key=" + localeMapKey + "&ip=" + statsRecord.getRemoteAddr());
            //将得到的String转化为JSON数据
            JSONObject localeJsonObject = JSON.parseObject(localeRet);
            //获得里面的code，根据返回值判断是否成功
            String infoCode = localeJsonObject.getString("infocode");
            LinkLocaleStatsDO linkLocaleStatsDO;
            if(StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode,"10000")){
                String province = localeJsonObject.getString("province");
                boolean unknownFlag = StrUtil.isBlank(province);
                linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .adcode(unknownFlag?"未知":localeJsonObject.getString("adcode"))
                        .cnt(1)
                        .city(unknownFlag?"未知":localeJsonObject.getString("city"))
                        .country("中国")
                        .date(new Date())
                        .province(unknownFlag?"未知":province)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleStats(linkLocaleStatsDO);

                //获取os
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .date(new Date())
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .os(LinkUtil.getOs(statsRecord.getOs()))
                        .cnt(1)
                        .build();
                linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
                //获取浏览器信息
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .browser(statsRecord.getBrowser())
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);

                //网络统计
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .network(statsRecord.getNetwork())
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

                //设备统计
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .device(statsRecord.getDevice())
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
                //访问日志监控实体

                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .user(statsRecord.getUv())
                        .ip(statsRecord.getRemoteAddr())
                        .browser(statsRecord.getBrowser())
                        .os(statsRecord.getOs())
                        .network(statsRecord.getNetwork())
                        .device(statsRecord.getDevice())
                        .locale(StrUtil.join("-","中国",linkLocaleStatsDO.getProvince(),linkLocaleStatsDO.getCity()))
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);
                baseMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);

                LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .todayPv(1)
                        .todayUv(statsRecord.getUvFirstFlag() ? 1 : 0)
                        .todayUip(statsRecord.getUvFirstFlag() ? 1 : 0)
                        .build();
                linkStatsTodayMapper.shortLinkTodayStats(linkStatsTodayDO);
            }
        }catch (Exception e){
            throw new ClientException("短链接统计异常");
        }finally {
            rLock.unlock();
        }
    }

    /**
     * 内部生成6位小短码用
     * @param requestParam 内部生成6位小短码用
     * @return 内部生成6位小短码用
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam){
        String suffix;
        //防止生成过多次
        int customeGenerateCount = 0;
        while(true){
            if(customeGenerateCount > 10){
                throw new ServiceException("短链接频繁生成，请稍后再试！");
            }
            //生成短链接
            String originUrl = requestParam.getOriginUrl();
            //加当前毫秒数，防止重复按一个东西生成，但是这么做，在大规模并发场景下，容易出现重复生成URL
            suffix = HashUtil.hashToBase62(originUrl+System.currentTimeMillis());
            if(!linkCreateRegisterCachePenetrationBloomFilter.contains(defaultDomain+"/"+suffix)){
                break;
            }
            customeGenerateCount++;
            /*查询数据库内是否存在
            LambdaQueryWrapper lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getDomain()+"/"+suffix);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(lambdaQueryWrapper);
            if(shortLinkDO == null){
                break;
            }
            这么做会导致大Key问题，因此需要布隆过滤器 */
        }

        //返回hashUtil生成对应的suffix
        return suffix;

    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }

}
