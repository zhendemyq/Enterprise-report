package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.UserDTO;
import com.example.backend.dto.UserQueryDTO;
import com.example.backend.service.UserService;
import com.example.backend.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户CRUD、密码管理等接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody UserDTO userDTO) {
        return Result.success(userService.createUser(userDTO));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<UserVO>> pageUsers(UserQueryDTO queryDTO) {
        IPage<UserVO> page = userService.pageUsers(queryDTO);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestParam Long userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/password/reset/{userId}")
    public Result<Void> resetPassword(@PathVariable Long userId) {
        userService.resetPassword(userId);
        return Result.success();
    }
}
