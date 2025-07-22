package molu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import molu.dao.entity.UserDO;
import molu.dto.req.UserLoginReqDTO;
import molu.dto.req.UserRegisterReqDTO;
import molu.dto.req.UserUpdateReqDTO;
import molu.dto.resp.UserLoginRespDTO;
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

    /**
     * 注册用户
     * @param requestParam 注册用户请求参数
     */
    void Register(UserRegisterReqDTO requestParam);

    /**
     * 根据用户名修改用户
     * @param requestParam
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam
     * @return
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查是否登录
     * @param token
     * @return
     */
    Boolean checklogin(String username,String token);
}
