package molu.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import molu.dao.entity.LinkStatsTodayDO;
import molu.dao.mapper.LinkStatsTodayMapper;
import molu.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}
