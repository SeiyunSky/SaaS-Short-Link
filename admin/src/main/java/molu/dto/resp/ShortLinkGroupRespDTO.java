package molu.dto.resp;

import lombok.Data;

/**
 * 查询短链接分组返回实体
 */
@Data
public class ShortLinkGroupRespDTO {
    private String gid;

    private String name;

    private int sortOrder;

    /**
     * 当前分组下短链接数量
     */
    private Integer shortLinkCount;
}
