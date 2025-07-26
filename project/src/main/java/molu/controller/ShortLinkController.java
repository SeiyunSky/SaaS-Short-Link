package molu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.req.ShortLinkPageReqDTO;
import molu.dto.req.ShortLinkUpdateReqDTO;
import molu.dto.resp.ShortLinkCountQueryRespDTO;
import molu.dto.resp.ShortLinkCreateRespDTO;
import molu.dto.resp.ShortLinkPageRespDTO;
import molu.service.ShortLinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     * @return
     */
    @PostMapping("/api/shortlink/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        shortLinkService.createShortLink(requestParam);
        return Results.success(null);
    }

    /**
     * 修改短链接分组
     * @param requestParam
     * @return
     */
    @PutMapping("/api/shortlink/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkService.update(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @GetMapping("/api/shortlink/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){

        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询分组内数量
     * @param
     * @return
     */
    @GetMapping("/api/shortlink/v1/count")
    public Result<List<ShortLinkCountQueryRespDTO>> groupShortLinkCount(@RequestParam List<String> requestParam){
        return Results.success(shortLinkService.groupShortLinkCount(requestParam));
    }
}
