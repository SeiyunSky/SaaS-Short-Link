package molu.controller;

import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.service.UrlTitleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL标题控制层
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {

    private final UrlTitleService urlTitleService;

    /**
     * 根据Url获取对应网站标题
     * @param url url
     * @return 标题
     */
    @GetMapping("/api/short-link/v1/title")
    public Result<String> getTitleByUrl(@RequestParam String url) {
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
