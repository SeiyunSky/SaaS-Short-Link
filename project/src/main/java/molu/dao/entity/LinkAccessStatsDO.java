package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import molu.common.database.BaseDO;

import java.util.Date;


/**
 * 短链接基础访问监控实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_access_stats")
public class LinkAccessStatsDO extends BaseDO {
    private Long id;

    private String fullShortUrl;

    private String gid;

    private Date date;

    private Integer pv;

    private Integer uv;

    private Integer uip;

    private Integer hour;

    private Integer weekday;
}