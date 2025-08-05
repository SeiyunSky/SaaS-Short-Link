package molu.remote.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import molu.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import molu.remote.dto.req.ShortLinkStatsReqDTO;
import molu.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import molu.remote.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkStatsService {

    default  IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam){
        return null;
    };
}
