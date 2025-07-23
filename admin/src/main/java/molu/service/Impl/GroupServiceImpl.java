package molu.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import molu.dao.entity.GroupDO;
import molu.dao.mapper.GroupMapper;
import molu.service.GroupService;
import org.springframework.stereotype.Service;

/**
 * 短链接分组实现层
 */
@Service
@Slf4j
//todo extends是什么意思
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

}
