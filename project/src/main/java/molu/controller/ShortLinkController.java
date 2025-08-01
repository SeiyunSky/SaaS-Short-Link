package molu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @GetMapping("/{short-uri:[a-zA-Z0-9]{6}}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, HttpServletRequest request, HttpServletResponse response) {
        shortLinkService.restoreUrl(shortUri,request,response);
    }

    /**
     * 创建短链接
     * @return 参数
     */
    @PostMapping("/api/shortlink/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.createShortLink(requestParam));

    }

    /**
     * 修改短链接分组
     * @param requestParam 参数
     * @return 参数
     */
    @PutMapping("/api/shortlink/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkService.update(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam 参数
     * @return 参数
     */
    @GetMapping("/api/shortlink/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){

        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询分组内数量
     * @param requestParam 参数
     * @return 参数
     */
    @GetMapping("/api/shortlink/v1/count")
    public Result<List<ShortLinkCountQueryRespDTO>> groupShortLinkCount(@RequestParam List<String> requestParam){
        return Results.success(shortLinkService.groupShortLinkCount(requestParam));
    }

}
