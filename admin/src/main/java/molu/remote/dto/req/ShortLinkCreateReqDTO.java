package molu.remote.dto.req;

import lombok.Data;
import molu.common.database.BaseDO;

import java.util.Date;

/**
 * 短链接创建请求对象
 */
@Data
public class ShortLinkCreateReqDTO extends BaseDO {

    /**
     * 域名
     */
    private String domain;

    /**
     * 原始链接
     */
    private String originUrl;


    /**
     * 分组标识
     */
    private String gid;

    /**
     * 创建类型
     */
    private Integer createdType;

    /**
     * 0：永久有效 1：自定义时长有效
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 描述
     */
    private String describe;
}
