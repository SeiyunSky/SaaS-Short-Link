package molu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import molu.common.convention.result.Result;
import molu.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import molu.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * URL回收站接口层
 */
public interface RecycleBinService {

    Result<IPage<ShortLinkPageRespDTO>> pageShortLinkRecycleOne(ShortLinkRecycleBinPageReqDTO requestParam);
}
