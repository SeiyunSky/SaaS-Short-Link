package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import molu.common.convention.exception.ClientException;
import molu.common.convention.exception.ServiceException;
import molu.dao.entity.ShortLinkDO;
import molu.dao.entity.ShortLinkGotoDO;
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
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import molu.service.ShortLinkService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 短链接实现层
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final ShortLinkGotoMapper shortLinkGoToMapper;
    private final RBloomFilter<String> linkCreateRegisterCachePenetrationBloomFilter;
    private final ShortLinkMapper shortLinkMapper;

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
            } else {
                LambdaUpdateWrapper<ShortLinkDO> ret = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, shortLink.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getEnableStatus, 0)
                        .set(false, ShortLinkDO::getValidDate, null);

                baseMapper.delete(ret);
                shortLinkDO.setGid(requestParam.getGid());
                baseMapper.insert(shortLinkDO);
            }
        }else {
            throw new ClientException("短链接不存在");
        }
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        //TODO 这里的拼接是我为了能让程序跑起来临时根据当前域名拼接的，实际上还需要做改动
        //TODO 原则上应该是域名不含http，domain里面存一个，拼接的数据应该符合domain+短链接
        String fullShortUrl = request.getServerName()+":"+request.getServerPort()+"/"+shortUri;
        LambdaQueryWrapper<ShortLinkGotoDO> wrapper =Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);

        ShortLinkGotoDO shortLinkGotoDO = shortLinkGoToMapper.selectOne(wrapper);
        if(shortLinkGotoDO == null) {
            //TODO 需要进行风控
            return;
        }
        LambdaQueryWrapper<ShortLinkDO> ret = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO shortLinkDO = baseMapper.selectOne(ret);

        if(shortLinkDO != null) {
            response.sendRedirect(shortLinkDO.getOriginUrl());
        }
    }

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
