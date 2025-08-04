package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import molu.common.database.BaseDO;

/**
 * 高频ip访问统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_access_logs")
public class LinkAccessLogsDO extends BaseDO {
    private Long id;

    private String fullShortUrl;

    private String gid;

    private String user;

    private String browser;

    private String os;

    private String ip;

    private String locale;

    private String device;

    private String network;
}
