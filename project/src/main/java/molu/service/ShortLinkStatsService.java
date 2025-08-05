package molu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import molu.dto.req.ShortLinkStatsAccessRecordReqDTO;
import molu.dto.req.ShortLinkStatsReqDTO;
import molu.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import molu.dto.resp.ShortLinkStatsRespDTO;

public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
}
