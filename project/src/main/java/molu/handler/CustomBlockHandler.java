package molu.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import molu.common.convention.result.Result;
import molu.dto.req.ShortLinkCreateReqDTO;
import molu.dto.resp.ShortLinkCreateRespDTO;

public class CustomBlockHandler {
    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException exception) {
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}
