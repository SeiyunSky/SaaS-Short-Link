package molu.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.dto.req.UserLoginReqDTO;
import molu.dto.req.UserRegisterReqDTO;
import molu.dto.req.UserUpdateReqDTO;
import molu.dto.resp.UserLoginRespDTO;
import molu.dto.resp.UserResponseDTO;
import molu.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;


    /**
     * 根据用户名查询用户
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserResponseDTO> getUser(@PathVariable String username) {
        UserResponseDTO result = userService.getUserByUsername(username);
        return Results.success(result);
    }

    /**
     * 查询用户名是否存在
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/user/has-username")
    public Result<Boolean> hasUserName(@RequestParam String username){
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 用户注册
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/user")
    public  Result<Void> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.Register(requestParam);
        return Results.success();
    }

    /**
     * 用户修改
     * @param requestParam
     * @return
     */
    @PutMapping("/api/shortlink/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam){
        userService.update(requestParam);
        return Results.success();
    }

    /**
     * 用户登录
     * @param requestParam
     * @return
     */
    @PostMapping("/api/shortlink/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam){
        return Results.success(userService.login(requestParam));
    }

    /**
     * 检查是否登录
     * @param token
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/user/check-login")
    public Result<Boolean> login(@RequestParam("username") String username,@RequestParam("token") String token){
        return Results.success(userService.checklogin(username,token));
    }

    /**
     * 退出登录
     * @param username
     * @return
     */
    @DeleteMapping("/api/shortlink/v1/user/check-logout")
    public Result<Void> logout(@RequestParam("username") String username,@RequestParam("token") String token){
        userService.logout(username,token);
        return Results.success();
    }
}