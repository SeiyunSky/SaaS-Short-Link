package molu.remote.dto;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import molu.common.convention.result.Result;
import molu.remote.dto.req.ShortLinkCreateReqDTO;
import molu.remote.dto.req.ShortLinkPageReqDTO;
import molu.remote.dto.req.ShortLinkUpdateReqDTO;
import molu.remote.dto.resp.ShortLinkCountQueryRespDTO;
import molu.remote.dto.resp.ShortLinkCreateRespDTO;
import molu.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短链接请求
     * @param requestParam 参数
     * @return 响应
     */
    default Result<ShortLinkCreateRespDTO> createLink(ShortLinkCreateReqDTO requestParam){
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<Result<ShortLinkCreateRespDTO>>() {
        });
    };

    /**
     * 分页查询短链接
     * @param requestParam 参数
     * @return 响应
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        //用hutool的httpUtil解决
        Map<String,Object> map = new HashMap<>();
        map.put("gid",requestParam.getGid());
        map.put("current",requestParam.getCurrent());
        map.put("size",requestParam.getSize());

        //获得json字符串
        String retPageStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/page",map);

        return JSON.parseObject(retPageStr,new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>(){
        });
    }

    /**
     * 修改短链接分组
     * @param requestParam 修改
     */
    default void update(ShortLinkUpdateReqDTO requestParam){
        // Hutool的HttpUtil.post()是最简单快捷的调用方式
        // 用HttpUtil.post()比构造PUT请求更方便（如Hutool工具类）  HttpUtil.post(url, body);
        // 对比需要额外配置的PUT：HttpRequest.put(url).body(body).execute(
        HttpRequest.put("http://127.0.0.1:8001/api/shortlink/v1/update")
                .body(JSON.toJSONString(requestParam))
                .execute();
        //String retStr = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/update",JSON.toJSONString(requestParam));
    };
    /**
     * 短链接分组内数量
     * @param requestParam 参数
     * @return 响应
     */
    default Result<List<ShortLinkCountQueryRespDTO>> groupShortLinkCount(List<String> requestParam){
        Map<String,Object> map = new HashMap<>();
        map.put("requestParam",requestParam);
        String retStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/count",map);
        return JSON.parseObject(retStr,new TypeReference<>(){
        });
    }

}
