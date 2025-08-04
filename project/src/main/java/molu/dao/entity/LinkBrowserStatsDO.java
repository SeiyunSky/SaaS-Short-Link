package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import molu.common.database.BaseDO;

import java.util.Date;

/**
 * 浏览器统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_browser_stats")
public class LinkBrowserStatsDO extends BaseDO{

    private Long id;

    private String fullShortUrl;

    private String gid;

    private Date date;

    private Integer cnt;

    private String browser;
}