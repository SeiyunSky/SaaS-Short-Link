package molu.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import molu.common.database.BaseDO;

/**
 * 短链接分组实体
 */
@Data
@TableName("t_group")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDO extends BaseDO {
    private Long id;

    private String gid;

    private String name;

    private String username;

    private int sortOrder;
}
