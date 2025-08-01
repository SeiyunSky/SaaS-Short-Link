package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import molu.common.convention.exception.ClientException;
import molu.dao.entity.ShortLinkDO;
import molu.dao.mapper.ShortLinkMapper;
import molu.dto.req.*;
import molu.dto.resp.ShortLinkPageRespDTO;
import molu.service.RecycleBinService;
import molu.toolkit.LinkUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static molu.common.constant.RedisKeyConstant.GOTO_IS_NULL_KEY;
import static molu.common.constant.RedisKeyConstant.GOTO_KEY;
import static molu.common.convention.errorcode.BaseErrorCode.CLIENT_ERROR;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     *
     * @param requestParam 请求参数
     */
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,0)
                .eq(ShortLinkDO::getDelFlag,0);

        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();

        baseMapper.update(shortLinkDO, queryWrapper);

        stringRedisTemplate.delete(String.format(GOTO_KEY,requestParam.getFullShortUrl()));

    }

    /**
     *
     * @param requestParam 分页数据
     * @return 返回
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, requestParam.getGidList())
                .eq(ShortLinkDO::getEnableStatus,1)
                .eq(ShortLinkDO::getDelFlag,0)
                .orderByDesc(ShortLinkDO::getCreateTime);

        IPage<ShortLinkDO> ret = baseMapper.selectPage(requestParam,queryWrapper);
        return ret.convert(each-> BeanUtil.toBean(each,ShortLinkPageRespDTO.class));

    }

    @Transactional
    @Override
    public void recoverShortLink(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus,1)
                .eq(ShortLinkDO::getDelFlag,0);

        ShortLinkDO existingLink = baseMapper.selectOne(queryWrapper);
        if (existingLink == null) {
            throw new ClientException(CLIENT_ERROR);
        }

        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(0)
                .build();

        int updatedRows = baseMapper.update(shortLinkDO, queryWrapper);

        if (updatedRows == 0) {
            throw new ClientException("短链接恢复失败");
        }

        //进行短链接预热，并且将对应的空白Key删掉
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_KEY,requestParam.getFullShortUrl()));

        stringRedisTemplate.opsForValue().set(String.format(GOTO_KEY,requestParam.getFullShortUrl()), existingLink.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(existingLink.getValidDate()),TimeUnit.MILLISECONDS);
    }

    @Transactional
    @Override
    public void deleteShortLink(RecycleBinDeleteReqDTO requestParam) {
        ShortLinkDO entity = baseMapper.selectOne(new LambdaQueryWrapper<ShortLinkDO>()
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 1)
        );
        if (entity== null) {
            throw new ClientException(CLIENT_ERROR);
        }
        // 触发自动逻辑删除
        baseMapper.deleteById(entity.getId());
    }
}
