package molu.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import molu.dao.entity.LinkLocaleStatsDO;
import molu.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区访问实体持久层
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    @Insert("insert into t_link_locale_stats(full_short_url,gid,date,cnt,province,city,adcode,country,create_time,update_time,del_flag) "+
    "values (#{fullShortUrl},#{gid},#{date},#{cnt},#{province},#{city},#{adcode},#{country}, NOW(), NOW(), 0)"+
    "ON DUPLICATE KEY UPDATE cnt  = cnt +#{cnt}, update_time = NOW(); ")
    void shortLinkLocaleStats(LinkLocaleStatsDO build1);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    province, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_locale_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, province;")
    List<LinkLocaleStatsDO> listLocaleByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}
