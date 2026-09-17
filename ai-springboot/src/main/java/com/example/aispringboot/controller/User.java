package com.example.aispringboot.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.aispringboot.DTO.command.UserRegisterCommandDTO;
import com.example.aispringboot.DTO.response.UserLoginResponseDTO;
import com.example.aispringboot.service.UserService;
import com.example.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import com.example.aispringboot.DTO.command.UserLoginCommandDTO;
import com.example.aispringboot.common.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class User {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO commandDTO) {
        //调用服务层方法
        return userService.login(commandDTO);
    }

    //用户注册接口
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO commandDTO){
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(commandDTO);
        return Result.success(result);
    }

    //获取当前用户
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        //如何从token中解析用户id
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        //调用service层获取用户详情
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserId(userId);
        return Result.success(result);
    }
}
