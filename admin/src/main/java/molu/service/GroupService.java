package molu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import molu.dao.entity.GroupDO;
import molu.dto.req.ShortLinkGroupSortReqDTO;
import molu.dto.req.ShortLinkGroupUpdateReqDTO;
import molu.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param groupname 短链接分组名
     */
    void saveGroup(String groupname);


    /**
     * 查询用户短链接分组集合
     * @return
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组名
     * @param requestParam
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     * @param gid
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
