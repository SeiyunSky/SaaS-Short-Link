package molu.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import molu.dao.entity.LinkOsStatsDO;
import molu.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接今日访问记录统计持久层
 */
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /**
     * 记录操作系统访问监控数据
     * @param linkStatsTodayDO 操作系统统计参数
     */
    @Insert("INSERT INTO t_link_stats_today (full_short_url, gid, date, today_pv,today_uv,today_uip, create_time, update_time, del_flag) " +
            "VALUES(#{linkTodayStats.fullShortUrl}, #{linkTodayStats.gid}, #{linkTodayStats.date}, #{linkTodayStats.todayUv},#{linkTodayStats.todayPv},#{linkTodayStats.todayUip}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE today_uv = today_uv + #{linkTodayStats.todayUv},today_pv = today_pv + #{linkTodayStats.todayPv},today_uip = today_uip + #{linkTodayStats.todayUip}, update_time = NOW()")
    void shortLinkTodayStats(@Param("linkTodayStats") LinkStatsTodayDO linkStatsTodayDO);

}
