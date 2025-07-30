package molu.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import molu.common.biz.user.UserContext;
import molu.common.convention.exception.ServiceException;
import molu.common.convention.result.Result;
import molu.common.enums.UserErrorCodeEnum;
import molu.dao.entity.GroupDO;
import molu.dao.mapper.GroupMapper;
import molu.remote.dto.ShortLinkRemoteService;
import molu.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import molu.remote.dto.resp.ShortLinkPageRespDTO;
import molu.service.RecycleBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * URL回收站实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLinkRecycleOne(ShortLinkRecycleBinPageReqDTO requestParam) {
        //查询当前用户下所有分组，然后把这个数据给过去
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if(CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException(UserErrorCodeEnum.USER_HAVE_NO_GROUP);
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkRemoteService.pageShortLinkRecycleOne(requestParam);
    }
}
