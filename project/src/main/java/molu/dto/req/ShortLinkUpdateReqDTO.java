package molu.dto.req;

import lombok.Data;
import molu.common.database.BaseDO;

import java.util.Date;

@Data
public class ShortLinkUpdateReqDTO extends BaseDO {
    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始分组标识
     */
    private String originGid;

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
