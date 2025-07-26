package molu.remote.dto.resp;

import lombok.Data;

/**
 * 短链接分组查询数量返回
 */
@Data
public class ShortLinkCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数量
     */
    private Integer count;

}
