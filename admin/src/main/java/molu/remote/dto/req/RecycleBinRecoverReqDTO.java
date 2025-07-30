package molu.remote.dto.req;

import lombok.Data;

/**
 * 回收站恢复功能
 */
@Data
public class RecycleBinRecoverReqDTO {

    private String gid;

    private String fullShortUrl;

}
