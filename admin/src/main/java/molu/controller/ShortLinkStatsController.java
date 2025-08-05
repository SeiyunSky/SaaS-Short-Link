package molu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.remote.dto.ShortLinkRemoteService;
import molu.remote.dto.ShortLinkStatsService;
import molu.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import molu.remote.dto.req.ShortLinkStatsReqDTO;
import molu.remote.dto.resp.ShortLinkStatsRespDTO;
import molu.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    ShortLinkStatsService shortLinkStatsService = new ShortLinkStatsService(){};
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService(){};

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStats(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }
}
