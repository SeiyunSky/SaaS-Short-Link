package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molu.Constant.RedisCacheConstant;
import molu.common.convention.exception.ClientException;
import molu.common.enums.UserErrorCodeEnum;
import molu.dao.entity.UserDO;
import molu.dao.mapper.UserMapper;
import molu.dto.req.UserRegisterReqDTO;
import molu.dto.resp.UserResponseDTO;
import molu.service.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


/**
 * 用户接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;

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

    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void Register(UserRegisterReqDTO requestParam) {

        if(!hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        //上锁
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER + requestParam.getUsername());

        //判断是否获取锁
        if(!lock.tryLock()) {
            throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
        }

        try {
                log.info("Register user : {}", requestParam.getUsername());
                int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if (inserted < 1) {
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        } finally{
            lock.unlock();
        }
    }
}
