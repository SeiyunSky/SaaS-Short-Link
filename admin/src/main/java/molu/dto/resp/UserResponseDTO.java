package molu.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import molu.common.serialize.PhoneDesensitizationSerializer;

/**
 * 用户返回参数响应
 */
@Data
public class UserResponseDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

}
