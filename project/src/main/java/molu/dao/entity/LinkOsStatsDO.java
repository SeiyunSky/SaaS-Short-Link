package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import molu.common.database.BaseDO;

import java.util.Date;

/**
 * 操作系统统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_os_stats")
public class LinkOsStatsDO extends BaseDO{

    private Long id;

    private String fullShortUrl;

    private String gid;

    private Date date;

    private Integer cnt;

    private String os;
}