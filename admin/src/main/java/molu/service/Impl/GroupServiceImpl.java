package molu.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import molu.common.biz.user.UserContext;
import molu.common.convention.result.Result;
import molu.common.database.BaseDO;
import molu.dao.entity.GroupDO;
import molu.dao.mapper.GroupMapper;
import molu.dto.req.ShortLinkGroupSortReqDTO;
import molu.dto.req.ShortLinkGroupUpdateReqDTO;
import molu.dto.resp.ShortLinkGroupRespDTO;
import molu.remote.dto.ShortLinkRemoteService;
import molu.remote.dto.resp.ShortLinkCountQueryRespDTO;
import molu.service.GroupService;
import molu.toolkit.RandomCodeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 短链接分组实现层
 */
@Service
@Slf4j
//todo extends是什么意思
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @Override
    public void saveGroup(String groupName) {
        String gid;

        do {
            gid = RandomCodeUtil.generateRandomCode();
        } while (hasGid(gid));

        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)
                .username(UserContext.getUsername())
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ShortLinkGroupRespDTO> listGroup() {
        // 1. 查询当前用户的分组列表
        LambdaQueryWrapper<GroupDO> ret = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag,0)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder,GroupDO::getUpdateTime);
        List<GroupDO> groupDOList = baseMapper.selectList(ret);

        // 2. 获取所有分组的gid列表
        List<String> gids = groupDOList.stream()
                .map(GroupDO::getGid)
                .collect(Collectors.toList());

        // 3. 批量查询每个分组的短链接数量,取出数据
        Result<List<ShortLinkCountQueryRespDTO>> countResult = shortLinkRemoteService.groupShortLinkCount(gids);
        List<ShortLinkCountQueryRespDTO> countList = countResult.getData();

        // 4. 将数量统计结果转换为Map便于查找 (gid -> count)
        Map<String, Integer> countMap = countList.stream()
                .collect(Collectors.toMap(
                        ShortLinkCountQueryRespDTO::getGid,
                        ShortLinkCountQueryRespDTO::getCount
                ));

        return groupDOList.stream()
                .map(group -> {
                    //这里复制分组的基础信息
                    ShortLinkGroupRespDTO respDTO = BeanUtil.copyProperties(group, ShortLinkGroupRespDTO.class);
                    //将对应短链接的数量输入进去
                    respDTO.setShortLinkCount(countMap.getOrDefault(group.getGid(), 0));
                    return respDTO;
                })
                .collect(Collectors.toList());
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

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .username(UserContext.getUsername())
                    .sortOrder(each.getSortOrder())
                    .gid(each.getGid())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(BaseDO::getDelFlag, 0);
            baseMapper.update(groupDO,updateWrapper);
        });
    }

    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper =Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO hasGroup = baseMapper.selectOne(queryWrapper);
        return hasGroup != null;
    }
}
