package molu.controller;

import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.dto.resp.UserResponseDTO;
import molu.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
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



}
