package molu.dto.resp;

import lombok.Data;

/**
 * 查询短链接分组返回实体
 */
@Data
public class ShortLinkGroupRespDTO {
    private String gid;

    private String name;

    private String username;

    private int sortOrder;
}
