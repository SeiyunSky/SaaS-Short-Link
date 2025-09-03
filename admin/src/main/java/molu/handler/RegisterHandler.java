package molu.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import molu.common.convention.result.Result;
import molu.dao.entity.UserDO;

public class RegisterHandler {

    // 全局限流处理
    public static Result<UserDO> handleFlowBlock(UserDO userDO, BlockException ex) {
        // 返回降级响应
        return new Result<UserDO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}
