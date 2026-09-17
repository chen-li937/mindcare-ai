package com.example.aispringboot.util;

import com.example.aispringboot.DTO.response.UserLoginResponseDTO;
import com.example.aispringboot.common.ResultCode;
import com.example.aispringboot.config.SecurityConfig;
import com.example.aispringboot.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthticationFilter extends OncePerRequestFilter {
    @Resource
    private UserService userService;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        //检查是否是公开的路径
        return SecurityConfig.isPublicPath(requestUri);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        // 1. 提取 token
        String token = JwtTokenUtil.extractTokenFromRequest(request);

        // 2. 没有 token → 拒绝
        if (!StringUtils.hasText(token)) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }

        // 3. 验证 token（过期/签名错误统一返回 401，避免 500）
        JwtTokenUtil.TokenVerificationResult validationResult;
        try {
            validationResult = JwtTokenUtil.validateToken(token);
        } catch (Exception e) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_EXPIRED);
            return;
        }
        if (validationResult == null || !validationResult.isValid()) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
            return;
        }

        // 4. 查询用户
        UserLoginResponseDTO.UserDetailResponseDTO user;
        try {
            user = userService.getUserId(validationResult.getUserId());
        } catch (Exception e) {
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.USER_NOT_EXIST);
            return;
        }

        // 5. 构建 Spring Security 认证对象放入上下文
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 6. 放行，finally 清理上下文
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}