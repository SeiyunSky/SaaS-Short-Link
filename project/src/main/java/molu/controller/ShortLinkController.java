package molu.controller;

import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.resp.ShortLinkCreateRespDTO;
import molu.service.ShortLinkService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
