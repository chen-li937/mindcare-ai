package com.example.aispringboot.service.convent;


import com.example.aispringboot.DTO.command.UserRegisterCommandDTO;
import com.example.aispringboot.DTO.response.UserLoginResponseDTO;
import com.example.aispringboot.entity.User;
import com.example.aispringboot.enumClass.UserStatus;
import com.example.aispringboot.enumClass.UserType;

import java.time.LocalDateTime;


public class UserConvent {
    //构建响应DTO
    public static UserLoginResponseDTO.UserDetailResponseDTO entiyToDetailResponse(User user) {
        return UserLoginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderDisplayName(user.getGender()))
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status(user.getStatus())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static User registerCommandToEntity(UserRegisterCommandDTO commandDTO, String encryptedPassword) {
        return User.builder()
                .username(commandDTO.getUsername())
                .password(encryptedPassword)
                .nickname(commandDTO.getNickname())
                .gender(commandDTO.getGender())
                .birthday(commandDTO.getBirthday())
                .phone(commandDTO.getPhone())
                .email(commandDTO.getEmail())
                .userType(UserType.fromCode(commandDTO.getUserType()).name())
                .status(UserStatus.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserLoginResponseDTO toLoginResponse(String token, UserLoginResponseDTO.UserDetailResponseDTO userInfo) {
        return UserLoginResponseDTO.builder()
                .userInfo(userInfo)
                .token(token)
                .userTypeDisplayName(userInfo.getUserTypeDisplayName())
                .build();
    }

    private static String getGenderDisplayName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }
}
