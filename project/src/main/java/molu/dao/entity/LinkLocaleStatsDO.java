package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import molu.common.database.BaseDO;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_locale_stats")
public class LinkLocaleStatsDO extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 城市的ad编码
     */
    private String adcode;

    /**
     * 所在国家（暂未启用）
     */
    private String country;

}