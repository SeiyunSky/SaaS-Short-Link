package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molu.constant.RedisCacheConstant;
import molu.common.convention.exception.ClientException;
import molu.common.enums.UserErrorCodeEnum;
import molu.dao.entity.UserDO;
import molu.dao.mapper.UserMapper;
import molu.dto.req.UserLoginReqDTO;
import molu.dto.req.UserRegisterReqDTO;
import molu.dto.req.UserUpdateReqDTO;
import molu.dto.resp.UserLoginRespDTO;
import molu.dto.resp.UserResponseDTO;
import molu.service.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * 用户接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

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
                try{
                    int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                    if (inserted < 1) {
                        throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                    }
                }catch (DuplicateKeyException ex){
                    throw new ClientException(UserErrorCodeEnum.USER_EXIST);
                }

                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        } finally{
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        //TODO 验证当前用户名是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam,UserDO.class),updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                        .eq(UserDO::getUsername, requestParam.getUsername())
                        .eq(UserDO::getPassword, requestParam.getPassword())
                        .eq(UserDO::getDelFlag,0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);

        if(userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NOT_EXIST);
        }

        Boolean hasLogin = stringRedisTemplate.hasKey("login_"+requestParam.getUsername());
        if(hasLogin!=null && hasLogin) {
            throw new ClientException(UserErrorCodeEnum.USER_LOGINING);
        }

        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_"+requestParam.getUsername(),uuid,JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_"+requestParam.getUsername(),30L,TimeUnit.MINUTES);

        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checklogin(String username,String token) {
        return stringRedisTemplate.opsForHash().get("login_"+username,token)!=null;
    }

    @Override
    public void logout(String username, String token) {
        if(checklogin(username,token)) {
            stringRedisTemplate.delete("login_" + username);
            return;
        }else {
            throw new ClientException(UserErrorCodeEnum.USER_NOT_EXIST);
        }
    }
}
