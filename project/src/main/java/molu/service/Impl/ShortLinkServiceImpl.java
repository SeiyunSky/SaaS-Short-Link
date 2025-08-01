package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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
import molu.dao.entity.LinkAccessStatsDO;
import molu.dao.entity.ShortLinkDO;
import molu.dao.entity.ShortLinkGotoDO;
import molu.dao.mapper.LinkAccessStatsMapper;
import molu.dao.mapper.ShortLinkGotoMapper;
import molu.dao.mapper.ShortLinkMapper;
import lombok.extern.slf4j.Slf4j;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.req.ShortLinkPageReqDTO;
import molu.dto.req.ShortLinkUpdateReqDTO;
import molu.dto.resp.ShortLinkCountQueryRespDTO;
import molu.dto.resp.ShortLinkCreateRespDTO;
import molu.dto.resp.ShortLinkPageRespDTO;
import molu.toolkit.HashUtil;
import molu.toolkit.LinkUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import molu.service.ShortLinkService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static molu.common.constant.RedisKeyConstant.*;

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

    /**
     * 创建短链接
     * @param requestParam 创建
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        //创建短链接
        String ret = generateSuffix(requestParam);
        String fullshortLink = StrBuilder
                .create(requestParam.getDomain())
                .append("/")
                .append(ret)
                .toString();
        ShortLinkDO shortLinkDO =ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .describe(requestParam.getDescribe())
                .shortUri(ret)
                .enableStatus(0)
                .fullShortUrl(fullshortLink)
                .build();

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
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,0)
                .eq(ShortLinkDO::getDelFlag,0)
                .orderByDesc(ShortLinkDO::getCreateTime);

        IPage<ShortLinkDO> ret = baseMapper.selectPage(requestParam,queryWrapper);

        return ret.convert(each->BeanUtil.toBean(each,ShortLinkPageRespDTO.class));

    }

    @Override
    public List<ShortLinkCountQueryRespDTO> groupShortLinkCount(List<String> requestParam) {
        return shortLinkMapper.groupShortLinkCount(requestParam);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> wrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,0)
                .last("FOR UPDATE NOWAIT"); //这是一个非阻塞式行锁
        ShortLinkDO shortLink =baseMapper.selectOne(wrapper);

        if(shortLink!=null) {
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(shortLink.getDomain())
                    .shortUri(shortLink.getShortUri())
                    .clickNum(shortLink.getClickNum())
                    .favicon(shortLink.getFavicon())
                    .gid(shortLink.getGid())
                    .createdType(shortLink.getCreatedType())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDate(requestParam.getValidDate())
                    .build();

            if (Objects.equals(shortLink.getGid(), requestParam.getGid())) {
                LambdaUpdateWrapper<ShortLinkDO> ret = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, requestParam.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getEnableStatus, 0)
                        .set(false, ShortLinkDO::getValidDate, null);

                baseMapper.update(shortLinkDO, ret);
                //清除缓存内旧东西，放入新的
                stringRedisTemplate.delete(String.format(GOTO_KEY,shortLink.getFullShortUrl()));
                stringRedisTemplate.opsForValue()
                        .set(String.format(GOTO_KEY,requestParam.getFullShortUrl()), shortLinkDO.getOriginUrl(),
                                LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),TimeUnit.MILLISECONDS
                        );

            } else {
                LambdaUpdateWrapper<ShortLinkDO> ret = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, shortLink.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getEnableStatus, 0)
                        .set(false, ShortLinkDO::getValidDate, null);

                baseMapper.delete(ret);
                stringRedisTemplate.delete(String.format(GOTO_KEY,shortLink.getFullShortUrl()));
                stringRedisTemplate.opsForValue()
                        .set(String.format(GOTO_KEY,requestParam.getFullShortUrl()), shortLinkDO.getOriginUrl(),
                                LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()),TimeUnit.MILLISECONDS
                        );
                shortLinkDO.setGid(requestParam.getGid());
                baseMapper.insert(shortLinkDO);
            }
        }else {
            throw new ClientException("短链接不存在");
        }
    }

    /**
     * 核心功能，跳转
     * @param shortUri 短链接后缀
     * @param request 请求
     * @param response 响应
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        //TODO 这里的拼接是我为了能让程序跑起来临时根据当前域名拼接的，实际上还需要做改动
        //TODO 原则上应该是域名不含http，domain里面存一个，拼接的数据应该符合domain+短链接
        String fullShortUrl = request.getServerName()+":"+request.getServerPort()+"/"+shortUri;
        //在Redis缓存里查询数据
        String originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_KEY,fullShortUrl));

        if(StrUtil.isNotBlank(originLink)){
            shortLinkStats(fullShortUrl, null, request,response);
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

        // 没找到的话，加分布式锁，防止缓存击穿
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_KEY,fullShortUrl));
        lock.lock();

        try {
            // 双检锁：再次检查缓存，因为阻塞的进程在首个完成查找更新缓存后，剩下的进程不用再去数据库找
            originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_KEY,fullShortUrl));
            if(StrUtil.isNotBlank(originLink)){
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
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request,response);
            response.sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            lock.unlock(); // 释放锁
        }
    }

    private void shortLinkStats(String fullShortUrl,String gid,HttpServletRequest request, HttpServletResponse response){
        //绕过 Java Lambda 表达式对外部局部变量“事实最终”的限制
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        //获取请求内的所有cookie
        Cookie[] cookies = request.getCookies();

        try {
            Runnable addCookieTask = ()->{
                //生成一个UUID作为用户标识，并存入Cookie中
                String uv = UUID.fastUUID().toString();
                Cookie cookie = new Cookie("uv",uv);
                cookie.setMaxAge(60*60*24*30);
                //动态设置Cookie的Path,使其匹配当前短链接的路径
                cookie.setPath(StrUtil.sub(fullShortUrl,fullShortUrl.indexOf("/"),fullShortUrl.length()));
                //将创建好的 Cookie 添加到 HTTP 响应中，使得浏览器能够接收并存储这个 Cookie
                response.addCookie(cookie);
                uvFirstFlag.set(Boolean.TRUE);
            };
            //做一层判断
            if(ArrayUtil.isNotEmpty(cookies)){
                Arrays.stream(cookies)
                        //过滤出名字是uv的cookie，并且获得第一个匹配的cookie，提取之
                        .filter(each->Objects.equals(each.getName(),"uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        //如果存在
                        .ifPresentOrElse(each->{
                            //TODO 用redis实现明显可以优化
                            Long add = stringRedisTemplate.opsForSet().add("shortlink:stats:uv" + fullShortUrl, each);
                            //// 若Redis返回1表示新增，0表示已存在
                            uvFirstFlag.set(add!=null&&add>0L);
                        },
                        // 不存在uv Cookie时的处理
                        addCookieTask);
            }else {
                addCookieTask.run();
            }

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

            LinkAccessStatsDO build = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get()?1:0)
                    .uip(1)
                    .hour(hour)
                    .weekday(value)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(DateUtil.beginOfDay(new Date()))
                    .build();
            linkAccessStatsMapper.shortLinkStats(build) ;
        }catch (Throwable e){
            log.error("短链接访问量统计异常");
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
            if(!linkCreateRegisterCachePenetrationBloomFilter.contains(requestParam.getDomain()+"/"+suffix)){
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
}
