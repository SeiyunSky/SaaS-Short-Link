package molu.common.enums;

import molu.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    USER_TOKEN_Fail("A000200","身份验证失败"),
    USER_NOT_EXIST("B000200","用户记录不存在"),
    USER_NAME_EXIST("B000201","用户名已存在"),
    USER_SAVE_ERROR("B000203","用户记录新增失败"),
    USER_LOGINING("B000204","用户已登录"),
    USER_HAVE_NO_GROUP("B000205","用户暂无分组信息"),
    USER_EXIST("B000202","用户记录已存在");

    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
