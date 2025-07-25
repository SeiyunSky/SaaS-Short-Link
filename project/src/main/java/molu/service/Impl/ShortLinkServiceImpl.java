package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import molu.dao.entity.ShortLinkDO;
import molu.dao.mapper.ShortLinkMapper;
import lombok.extern.slf4j.Slf4j;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.resp.ShortLinkCreateRespDTO;
import molu.toolkit.HashUtil;
import org.springframework.stereotype.Service;
import molu.service.ShortLinkService;

/**
 * 短链接实现层
 */
@Service
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        //创建短链接
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setFullShortUrl(requestParam.getDomain()+"/"+generateSuffix(requestParam));

        baseMapper.insert(shortLinkDO);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(shortLinkDO.getGid())
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam){
        String originUrl = requestParam.getOriginUrl();
        //通过hashUtil生成对应的suffix
        return HashUtil.hashToBase62(originUrl);

    }
}
