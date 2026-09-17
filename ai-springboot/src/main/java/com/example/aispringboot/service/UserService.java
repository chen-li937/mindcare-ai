package com.example.aispringboot.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aispringboot.DTO.command.UserLoginCommandDTO;
import com.example.aispringboot.DTO.command.UserRegisterCommandDTO;
import com.example.aispringboot.DTO.response.UserLoginResponseDTO;
import com.example.aispringboot.common.Result;
import com.example.aispringboot.entity.User;
import com.example.aispringboot.enumClass.UserType;
import com.example.aispringboot.exception.BusinessException;
import com.example.aispringboot.mapper.UserMapper;
import com.example.aispringboot.service.convent.UserConvent;
import com.example.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Result<UserLoginResponseDTO> login(UserLoginCommandDTO commandDTO) {
        //构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, commandDTO.getUsername())
                .or()
                .eq(User::getEmail, commandDTO.getUsername());

        //调用MP API查询
        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);

        //判断用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        //验证密码
        String inputPassword = commandDTO.getPassword().trim();
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        //检查用户的状态
        if (!user.isActive()) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        //生成JWT token
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvent.entiyToDetailResponse(user);
        return Result.success(UserConvent.toLoginResponse(token, userInfo));
    }

    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO commandDTO) {
        //验证密码是否一致
        if (!commandDTO.getPassword().equals(commandDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }

        //检查用户名是否存在
        LambdaQueryWrapper<User> userNameQuery = new LambdaQueryWrapper<>();
        userNameQuery.eq(User::getUsername, commandDTO.getUsername());
        if (userMapper.selectCount(userNameQuery) > 0) {
            throw new BusinessException("用户名已存在");
        }

        //检查邮箱是否存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, commandDTO.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException("邮箱已存在");
        }

        //用户类型
        if (!UserType.isValidCode(commandDTO.getUserType())) {
            throw new BusinessException("无效的用户类型");
        }

        //创建用户
        String password = commandDTO.getPassword().trim();
        String encryptedPassword = passwordEncoder.encode(password);
        User user = UserConvent.registerCommandToEntity(commandDTO, encryptedPassword);

        //插入数据库
        userMapper.insert(user);

        return UserConvent.entiyToDetailResponse(user);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO getUserId(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserConvent.entiyToDetailResponse(user);
    }
}
