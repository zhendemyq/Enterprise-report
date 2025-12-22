package com.example.backend.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.backend.entity.SysRole;
import com.example.backend.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        List<String> roleList = new ArrayList<>();
        for (SysRole role : roles) {
            roleList.add(role.getRoleCode());
        }
        return roleList;
    }
}
