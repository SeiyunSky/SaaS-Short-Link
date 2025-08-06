package molu.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import molu.dao.entity.ShortLinkDO;
import molu.dto.resp.ShortLinkCountQueryRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 短链接持久层
 */
@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 分组内短链接数量
     * @param requestParam 参数
     */
    List<ShortLinkCountQueryRespDTO> groupShortLinkCount(List<String> requestParam);

    /**
     * 访问统计自增
     */
    void incrementStats(@Param("gid") String gid,@Param("fullShortUrl") String fullShortUrl,@Param("totalPv") Integer totalPv, @Param("totalUv") Integer totalUv,@Param("totalUip") Integer totalUip);
}
