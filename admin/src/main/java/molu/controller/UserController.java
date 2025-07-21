package molu.controller;

import lombok.RequiredArgsConstructor;
import molu.common.convention.result.Result;
import molu.common.convention.result.Results;
import molu.common.enums.UserErrorCodeEnum;
import molu.dto.resp.UserResponseDTO;
import molu.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
}
