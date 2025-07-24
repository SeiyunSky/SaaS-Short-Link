package molu.controller;

import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.dto.req.ShortLinkGroupSaveReqDTO;
import molu.dto.req.ShortLinkGroupUpdateReqDTO;
import molu.dto.resp.ShortLinkGroupRespDTO;
import molu.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新建短链接分组
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组
     * @return
     */
    @GetMapping("/api/shortlink/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup(){
        return Results.success( groupService.listGroup());
    }

    /**
     * 修改分组名
     * @param requestParam
     */
    @PutMapping("/api/shortlink/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接分组
     * @param gid
     * @return
     */
    @DeleteMapping("/api/shortlink/v1/group")
    public Result<Void> deleteGroup(@RequestParam String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }


}
