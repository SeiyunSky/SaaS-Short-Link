package molu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import molu.dao.entity.GroupDO;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param groupname 短链接分组名
     */
    void saveGroup(String groupname);

}
