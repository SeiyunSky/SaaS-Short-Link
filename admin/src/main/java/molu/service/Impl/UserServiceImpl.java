package molu.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import molu.common.convention.exception.ClientException;
import molu.common.enums.UserErrorCodeEnum;
import molu.dao.entity.UserDO;
import molu.dao.mapper.UserMapper;
import molu.dto.resp.UserResponseDTO;
import molu.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {


    @Override
    public UserResponseDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        UserResponseDTO userResponseDTO = new UserResponseDTO();

        if (userDO != null) {
            BeanUtils.copyProperties(userDO, userResponseDTO);
            return userResponseDTO;
        }else {
            throw new ClientException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
    }
}
