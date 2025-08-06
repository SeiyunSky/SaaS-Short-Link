package molu.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molu.common.convention.exception.ClientException;
import molu.common.enums.UserErrorCodeEnum;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    private static final List<String> IGNORE_URL = Lists.newArrayList(
            "/api/shortlink/admin/v1/user/login",
            "/api/shortlink/admin/v1/user/has-username",
            "/api/shortlink/admin/v1/user"
            );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();

        if(IGNORE_URL.stream().noneMatch(requestURI::startsWith)){
            String method = httpServletRequest.getMethod();
            if (!(Objects.equals(requestURI, "/api/shortlink/admin/v1/user")&&Objects.equals("POST", method))) {
                String username = httpServletRequest.getHeader("username");
                String token = httpServletRequest.getHeader("token");
                if(StrUtil.isAllBlank(username, token)){
                    //todo 全局异常拦截器拦截不到这里
                    throw new ClientException(UserErrorCodeEnum.USER_TOKEN_Fail);
                }

                Object userInfoJsonStr;
                try{
                    userInfoJsonStr = stringRedisTemplate.opsForHash().get("login_" + username, token);
                    if(userInfoJsonStr == null){
                        //todo 全局异常拦截器拦截不到这里
                        log.error("Failed to verify user token from Redis");
                        throw new ClientException(UserErrorCodeEnum.USER_TOKEN_Fail);
                    }
                }catch (Exception e){
                    //todo 全局异常拦截器拦截不到这里
                    throw new ClientException(UserErrorCodeEnum.USER_TOKEN_Fail);
                }

                    UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
                    UserContext.setUser(userInfoDTO);
            }
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}