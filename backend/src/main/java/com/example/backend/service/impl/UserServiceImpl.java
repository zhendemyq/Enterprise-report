package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.UserDTO;
import com.example.backend.dto.UserQueryDTO;
import com.example.backend.entity.User;
import com.example.backend.entity.UserRole;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.mapper.UserRoleMapper;
import com.example.backend.service.UserService;
import com.example.backend.vo.LoginVO;
import com.example.backend.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        User user = baseMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证密码
        String encryptPwd = SecureUtil.md5(loginDTO.getPassword());
        if (!encryptPwd.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 登录
        StpUtil.login(user.getId());

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(user);

        // 获取角色列表
        List<String> roles = baseMapper.selectRoleCodesByUserId(user.getId());

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(StpUtil.getTokenValue());
        loginVO.setRoles(roles);

        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        loginVO.setUserInfo(userVO);

        return loginVO;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO userDTO) {
        // 检查用户名是否存在
        User existUser = baseMapper.selectByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        // 创建用户
        User user = BeanUtil.copyProperties(userDTO, User.class);
        user.setPassword(SecureUtil.md5(userDTO.getPassword()));
        user.setStatus(1);
        save(user);

        // 保存用户角色关联
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            for (Long roleId : userDTO.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserDTO userDTO) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        BeanUtil.copyProperties(userDTO, user, "id", "username", "password");
        updateById(user);

        // 更新角色关联
        if (userDTO.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, id));
            for (Long roleId : userDTO.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(id);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public void deleteUser(Long id) {
        removeById(id);
    }

    @Override
    public IPage<UserVO> pageUsers(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名、昵称、邮箱）
        if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(User::getUsername, queryDTO.getKeyword())
                    .or().like(User::getNickname, queryDTO.getKeyword())
                    .or().like(User::getEmail, queryDTO.getKeyword())
            );
        }

        wrapper.like(StringUtils.isNotBlank(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername())
                .like(StringUtils.isNotBlank(queryDTO.getNickname()), User::getNickname, queryDTO.getNickname())
                .eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getDeptId() != null, User::getDeptId, queryDTO.getDeptId())
                .orderByDesc(User::getCreateTime);

        // 如果指定了角色ID，先查询该角色下的用户ID
        if (queryDTO.getRoleId() != null) {
            List<Long> userIds = baseMapper.selectUserIdsByRoleId(queryDTO.getRoleId());
            if (userIds.isEmpty()) {
                // 没有用户拥有该角色，返回空结果
                return new Page<UserVO>().setRecords(List.of()).setTotal(0);
            }
            wrapper.in(User::getId, userIds);
        }

        IPage<User> userPage = page(page, wrapper);
        return userPage.convert(user -> {
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            // 查询用户角色
            userVO.setRoles(baseMapper.selectRolesByUserId(user.getId()));
            return userVO;
        });
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        String encryptOldPwd = SecureUtil.md5(oldPassword);
        if (!encryptOldPwd.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        user.setPassword(SecureUtil.md5(newPassword));
        updateById(user);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 默认密码123456
        user.setPassword(SecureUtil.md5("123456"));
        updateById(user);
    }
}
