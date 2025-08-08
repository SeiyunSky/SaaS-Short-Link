package molu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.remote.dto.ShortLinkRemoteService;
import molu.remote.dto.req.ShortLinkBatchCreateReqDTO;
import molu.remote.dto.req.ShortLinkCreateReqDTO;
import molu.remote.dto.req.ShortLinkPageReqDTO;
import molu.remote.dto.req.ShortLinkUpdateReqDTO;
import molu.remote.dto.resp.ShortLinkBaseInfoRespDTO;
import molu.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import molu.remote.dto.resp.ShortLinkCreateRespDTO;
import molu.remote.dto.resp.ShortLinkPageRespDTO;
import molu.toolkit.EasyExcelWebUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
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
