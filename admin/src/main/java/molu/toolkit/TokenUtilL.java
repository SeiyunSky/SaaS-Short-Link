package molu.toolkit;

import lombok.RequiredArgsConstructor;
import molu.common.convention.exception.ClientException;
import molu.common.enums.UserErrorCodeEnum;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
@RequiredArgsConstructor
public class TokenUtilL {
    private static StringRedisTemplate stringRedisTemplate;
    /**
     * 刷新 Token 的 Redis 缓存过期时间
     * @param username 用户名
     */
    public static void refreshToken(String username) {
        String loginKey = "login_" + username;

        // 检查 Token 是否存在
        if (stringRedisTemplate.hasKey(loginKey)) {
            // 延长整个 Key 的过期时间（续期 30 分钟）
            stringRedisTemplate.expire(loginKey, 30, TimeUnit.MINUTES);
        } else {
            throw new ClientException(UserErrorCodeEnum.USER_TOKEN_Fail);
        }
    }

}
