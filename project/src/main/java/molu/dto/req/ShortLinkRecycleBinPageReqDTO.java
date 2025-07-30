package molu.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import molu.dao.entity.ShortLinkDO;

import java.util.List;

@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {
    /**
     * 分组标识
     */
    private List<String> gidList;
}
