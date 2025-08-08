package molu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.remote.dto.req.RecycleBinDeleteReqDTO;
import molu.remote.dto.req.RecycleBinRecoverReqDTO;
import molu.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import molu.remote.dto.ShortLinkRemoteService;
import molu.remote.dto.req.RecycleBinSaveReqDTO;
import molu.remote.dto.resp.ShortLinkPageRespDTO;
import molu.service.RecycleBinService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 保存回收站功能
     * @param requestParam 反应
     * @return 反应
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam 参数
     * @return 参数
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam){
        return recycleBinService.pageShortLinkRecycleOne(requestParam);
    }

    /**
     * 恢复删除短链接
     * @param requestParam 参数
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRecoverReqDTO requestParam){
        shortLinkRemoteService.recoverShortLink(requestParam);
        return Results.success();
    }

    /**
     * 逻辑删除回收站短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> deleteShortLink(@RequestBody RecycleBinDeleteReqDTO requestParam){
        shortLinkRemoteService.deleteShortLink(requestParam);
        return Results.success();
    }
}
