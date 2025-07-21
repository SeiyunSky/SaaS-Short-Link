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
}
