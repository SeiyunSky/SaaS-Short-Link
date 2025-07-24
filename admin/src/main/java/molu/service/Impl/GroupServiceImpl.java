package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import molu.common.biz.user.UserContext;
import molu.common.database.BaseDO;
import molu.dao.entity.GroupDO;
import molu.dao.mapper.GroupMapper;
import molu.dto.req.ShortLinkGroupUpdateReqDTO;
import molu.dto.resp.ShortLinkGroupRespDTO;
import molu.service.GroupService;
import molu.toolkit.RandomCodeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 短链接分组实现层
 */
@Service
@Slf4j
//todo extends是什么意思
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;

        do {
            gid = RandomCodeUtil.generateRandomCode();
        } while (hasGid(gid));

        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> ret = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag,0)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder,GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(ret);
        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
    }

    @Transactional
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> ret = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(BaseDO::getDelFlag, 0);

        GroupDO groupDO =GroupDO.builder()
                .name(requestParam.getName())
                .build();
        log.info("传入数据为:{},{}",groupDO,ret);
        baseMapper.update(groupDO,ret);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> ret = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(BaseDO::getDelFlag, 0);

        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        log.info("传入数据为:{},{}",groupDO,ret);
        baseMapper.update(groupDO,ret);
    }

    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper =Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO hasGroup = baseMapper.selectOne(queryWrapper);
        return hasGroup != null;
    }
}
