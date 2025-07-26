package molu.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import molu.dao.entity.ShortLinkDO;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.req.ShortLinkPageReqDTO;
import molu.dto.req.ShortLinkUpdateReqDTO;
import molu.dto.resp.ShortLinkCountQueryRespDTO;
import molu.dto.resp.ShortLinkCreateRespDTO;
import molu.dto.resp.ShortLinkPageRespDTO;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询分组内数量
     * @param requestParam
     * @return
     */
    List<ShortLinkCountQueryRespDTO> groupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接
     * @param requestParam
     */
    void update(ShortLinkUpdateReqDTO requestParam);
}
