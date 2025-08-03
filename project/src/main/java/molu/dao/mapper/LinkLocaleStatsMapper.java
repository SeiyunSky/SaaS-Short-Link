package molu.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import molu.dao.entity.LinkLocaleStatsDO;
import org.apache.ibatis.annotations.Insert;

/**
 * 地区访问实体持久层
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    @Insert("insert into t_link_locale_stats(full_short_url,gid,date,cnt,province,city,adcode,country,create_time,update_time,del_flag) "+
    "values (#{fullShortUrl},#{gid},#{date},#{cnt},#{province},#{city},#{adcode},#{country}, NOW(), NOW(), 0)"+
    "ON DUPLICATE KEY UPDATE cnt  = cnt +#{cnt}; ")
    void shortLinkLocaleStats(LinkLocaleStatsDO build1);
}
