package com.edu.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.material.dto.LoginRequest;
import com.edu.material.dto.RegisterRequest;
import com.edu.material.entity.SysUser;
import com.edu.material.entity.SysRole;
import com.edu.material.entity.SysUserRole;
import com.edu.material.mapper.SysUserMapper;
import com.edu.material.mapper.SysRoleMapper;
import com.edu.material.mapper.SysUserRoleMapper;
import com.edu.material.util.JwtUtil;
import com.edu.material.vo.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    public LoginResponse login(LoginRequest request) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoleType()
        );

        return new LoginResponse(token, userInfo);
    }

    @Transactional
    public void register(RegisterRequest request) {
        SysUser existUser = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setRoleType(request.getRoleType());

        save(user);

        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, request.getRoleType()));
        
        if (role != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        }
    }

    public SysUser getUserInfo(Long userId) {
        return getById(userId);
    }
}