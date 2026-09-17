package com.example.aispringboot.common;

public enum ResultCode {
    //枚举类的第一项必须是枚举项
    SUCCESS("200", "操作成功"),

    ERROR("-1", "操作失败"),

    UNAUTHORIZED("401", "暂未登录或token已过期"),

    SYSTEM_ERROR("500", "系统错误"),

    //参数相关错误
    PARAM_ERROR("400","参数错误"),

    PARAM_MISSING("4001","参数缺失"),

    PARAM_INVALID("4002","参数无效"),

    //文件操作相关错误
    FILE_NOT_FOUND("5001","文件不存在"),

    FILE_UPLOAD_ERROR("5002","文件上传错误"),

    FILE_DELETE_ERROR("5003","文件删除错误"),

    FILE_SIZE_ERROR("5004","文件大小错误"),

    FILE_TYPE_NOT_SUPPORT("5005","文件类型不支持"),

    FILE_NAME_INVALID("5006","文件名无效"),

    FILE_CONTENT_INVALID("5007","文件内容无效"),

    FILE_SAVE_FAILED("5008","文件保存失败"),

    //业务相关错误
    BUSINESS_ERROR("6000","业务错误"),

    ACCOUNT_SAME("6001","账号已存在"),

    USER_NOT_EXIST("6002","用户不存在"),

    //token相关错误
    TOKEN_INVALID("A0230","token无效"),

    TOKEN_EXPIRED("A0230","token已过期"),

    TOKEN_BLACKLIST("A0230","token在黑名单中"),

    TOKEN_ACCESS_FORBIDDEN("A0231","token访问被禁止"),

    AUTHORIZED_ERROR("A0300","访问权限异常"),

    ACCESS_UNAUTHORIZED("A0301","访问未授权");

    private String code;
    private String msg;

    ResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}