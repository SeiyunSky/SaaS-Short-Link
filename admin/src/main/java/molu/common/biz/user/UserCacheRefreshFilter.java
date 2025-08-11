package molu.common.biz.user;
//  refreshToken(UserContext.getUsername());

import jakarta.servlet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class UserCacheRefreshFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    private static final long TOKEN_EXPIRE_TIME = 30L; // 30分钟

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        // 1. 获取当前用户信息
        String username = UserContext.getUsername();
        if (!StringUtils.hasText(username)) {
            // 如果用户上下文没有用户名，直接跳过刷新逻辑
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        try {
            // 2. 执行过滤器链
            filterChain.doFilter(servletRequest, servletResponse);

            // 3. 请求处理完成后刷新token
            refreshUserToken(username);

        } catch (Exception e) {
            log.error("用户缓存刷新失败 - username: {}", username, e);
            throw e;
        }
    }

    /**
     * 刷新用户token有效期
     * @param username 用户名
     */
    private void refreshUserToken(String username) {
        try {
            String loginKey = "login_" + username;

            // 检查key是否存在
            Boolean hasKey = stringRedisTemplate.hasKey(loginKey);
            if (hasKey) {
                // 刷新整个hash的过期时间
                stringRedisTemplate.expire(loginKey, TOKEN_EXPIRE_TIME, TimeUnit.MINUTES);
                log.debug("用户token刷新成功 - username: {}, key: {}", username, loginKey);
            } else {
                log.warn("用户token不存在，无法刷新 - username: {}, key: {}", username, loginKey);
            }
        } catch (Exception e) {
            log.error("刷新用户token异常 - username: {}", username, e);
        }
    }
}
