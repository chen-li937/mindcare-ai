package com.example.aispringboot.entity;

//用户实体类
import com.example.aispringboot.enumClass.UserStatus;
import com.example.aispringboot.enumClass.UserType;
import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
@Builder
public class User {
    //用户id
    @TableId(type = IdType.AUTO)
    private Long id;

    //用户名
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    //密码
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20之间")
    private String password;

    //昵称
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    //头像
    @Size(max = 255, message = "头像长度不能超过255")
    private String avatar;

    //性别
    private Integer gender;

    //生日
    private LocalDate birthday;

    //手机号
    @Size(max = 11, message = "手机号长度不能超过11")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    //邮箱
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    //用户类型 1：普通用户 2：管理员
    @TableField("usertype")
    private String userType;

    //状态 0：正常 1：禁用
    private Integer status;

    //创建时间
    @TableField("created_at")
    private LocalDateTime createdAt;

    //更新时间
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    //是否为普通用户
    public boolean isUser() {
        return UserType.USER.name().equals(this.userType);
        }

    //是否为正常状态
    public boolean isActive() {
        return UserStatus.NORMAL.getCode().equals(this.status);
    }

    //是否被禁用
    public boolean isDisabled() {
        return UserStatus.DISABLED.getCode().equals(this.status);
    }

    //获取显示名称（优先返回昵称，否则返回用户名）
    public String getDisplayName() {
        return nickname != null && !nickname.trim().isEmpty() ? nickname : username;
    }

    //获取用户类型显示名称
    public String getUserTypeDisplayName() {
        try {
            return UserType.valueOf(userType).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知角色";
        }
    }

    public String getStatusDisplayName() {
        try {
            return UserStatus.fromCode(status).getMessage();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }
}

