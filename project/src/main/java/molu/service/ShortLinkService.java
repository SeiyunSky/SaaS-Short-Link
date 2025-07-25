package molu.service;


import com.baomidou.mybatisplus.extension.service.IService;
import molu.dao.entity.ShortLinkDO;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.resp.ShortLinkCreateRespDTO;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);
}
