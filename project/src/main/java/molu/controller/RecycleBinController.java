package molu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.dto.req.*;
import molu.dto.resp.ShortLinkPageRespDTO;
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

    @PostMapping("/api/shortlink/v1/recyclebin/saver")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam 参数
     * @return 参数
     */
    @GetMapping("/api/shortlink/v1/recyclebin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam){

        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 恢复回收站短链接
     * @param requestParam 参数
     */
    @PostMapping("/api/shortlink/v1/recyclebin/recover")
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRecoverReqDTO requestParam){
        recycleBinService.recoverShortLink(requestParam);
        return Results.success();
    }

    /**
     * 逻辑删除回收站短链接
     */
    @PostMapping("/api/shortlink/v1/recyclebin/delete")
    public Result<Void> deleteShortLink(@RequestBody RecycleBinDeleteReqDTO requestParam){
        recycleBinService.deleteShortLink(requestParam);
        return Results.success();
    }
}
