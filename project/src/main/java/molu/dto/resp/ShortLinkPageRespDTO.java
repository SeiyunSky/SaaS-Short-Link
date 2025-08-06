package molu.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import molu.common.database.BaseDO;

import java.util.Date;

/**
 * 短链接分页返回参数
 */
@Data
public class ShortLinkPageRespDTO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 网站图标
     */
    private String favicon;

    /**
     * 0：永久有效 1：自定义时长有效
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date validDate;

    /**
     * 描述
     */
    private String describe;

    /**
     * 历史访问次数
     */
    private Integer totalPv;

    /**
     * 今日访问次数
     */
    private Integer todayPv;

    /**
     * 历史访问用户
     */
    private Integer totalUv;

    /**
     * 今日访问用户
     */
    private Integer todayUv;

    /**
     * 历史访问IP
     */
    private Integer totalUip;

    /**
     * 今日访问IP
     */
    private Integer todayUip;

}
