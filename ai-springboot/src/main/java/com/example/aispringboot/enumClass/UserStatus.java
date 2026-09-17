package com.example.aispringboot.enumClass;

import lombok.Getter;

@Getter
public enum UserStatus {
    DISABLED(0,"禁用"),
    NORMAL(1,"正常");;

    private final Integer code;
    private final String message;

    UserStatus(Integer code, String description) {
        this.code = code;
        this.message = description;
    }

    public static UserStatus fromCode(Integer code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException(" 未知的用户状态代码: " + code);
    }
}
