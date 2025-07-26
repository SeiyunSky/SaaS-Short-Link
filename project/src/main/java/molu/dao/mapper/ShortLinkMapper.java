package molu.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import molu.dao.entity.ShortLinkDO;
import molu.dto.resp.ShortLinkCountQueryRespDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 短链接持久层
 */
@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 分组内短链接数量
     * @param requestParam
     */
    List<ShortLinkCountQueryRespDTO> groupShortLinkCount(List<String> requestParam);
}
