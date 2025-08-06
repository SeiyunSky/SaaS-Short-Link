package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import molu.common.database.BaseDO;

import java.util.Date;

/**
 * 短链接今日访问记录统计实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_stats_today")
public class LinkStatsTodayDO extends BaseDO {

        private Long id;

        private String gid;

        private String fullShortUrl;

        private Date date;

        private Integer todayPv;

        private Integer todayUv;

        private Integer todayUip;

}
