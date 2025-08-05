package molu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.remote.dto.ShortLinkRemoteService;
import molu.remote.dto.req.ShortLinkCreateReqDTO;
import molu.remote.dto.req.ShortLinkPageReqDTO;
import molu.remote.dto.req.ShortLinkUpdateReqDTO;
import molu.remote.dto.resp.ShortLinkCreateRespDTO;
import molu.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 创建短链接
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return shortLinkRemoteService.createLink(requestParam);
    }

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 修改短链接分组
     * @param requestParam
     * @return
     */
    @PutMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkRemoteService.update(requestParam);
        return Results.success();
    }
}
