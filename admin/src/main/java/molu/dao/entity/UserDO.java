package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import molu.common.database.BaseDO;

import java.util.Date;

/**
 * 用户持久层实体
 */
@Data
@TableName("user")
public class UserDO extends BaseDO {

        /**
         * ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 邮箱
         */
        private String mail;

        /**
         * 注销时间戳
         */
        private Long deletionTime;
}
