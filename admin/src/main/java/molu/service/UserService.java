package molu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import molu.dao.entity.UserDO;
import molu.dto.resp.UserResponseDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户消息
     * @param username
     * @return
     */
    UserResponseDTO getUserByUsername(String username);

    /**
     * 查询用户名是否可用
     * @return 用户名是否存在
     */
    Boolean hasUsername(String username);

    void Register();
}
