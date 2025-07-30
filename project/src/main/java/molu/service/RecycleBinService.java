package molu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import molu.dao.entity.ShortLinkDO;
import molu.dto.req.RecycleBinRecoverReqDTO;
import molu.dto.req.RecycleBinSaveReqDTO;
import molu.dto.req.ShortLinkPageReqDTO;
import molu.dto.req.ShortLinkRecycleBinPageReqDTO;
import molu.dto.resp.ShortLinkPageRespDTO;


/**
 * 回收短链接接口层
 */
public interface RecycleBinService  extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 参数
     * @return 参数
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复短链接
     * @param requestParam 参数
     */
    void recoverShortLink(RecycleBinRecoverReqDTO requestParam);
}
