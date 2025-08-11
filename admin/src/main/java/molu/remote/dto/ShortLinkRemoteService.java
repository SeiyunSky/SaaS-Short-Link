package molu.remote.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import molu.common.convention.result.Result;
import molu.remote.dto.req.*;
import molu.remote.dto.resp.*;
import org.springframework.web.bind.annotation.RequestParam;

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
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
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
        map.put("orderTag", requestParam.getOrderTag());
        map.put("current",requestParam.getCurrent());
        map.put("size",requestParam.getSize());

        //获得json字符串
        String retPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page",map);

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
        HttpRequest.put("http://127.0.0.1:8001/api/short-link/v1/update")
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
        String retStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count",map);
        return JSON.parseObject(retStr,new TypeReference<>(){
        });
    }

    /**
     * 根据URL获取标题
     * @param url 原网站
     * @return 标题
     */
    default Result<String> getTitleByUrl(@RequestParam String url){
        String ret = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url="+url);
        return JSON.parseObject(ret,new TypeReference<>(){
        });
    }

    /**
     * 修改短链接启用禁用状态，保存回收站
     * @param requestParam RBSaveDTO
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recyclebin/save",JSON.toJSONString(requestParam));
    };

    /**
     * 分页查询回收站短链接
     * @param requestParam RB
     * @return 响应
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLinkRecycleOne(ShortLinkRecycleBinPageReqDTO requestParam){
        Map<String,Object> map = new HashMap<>();
        map.put("gidList",requestParam.getGidList());
        map.put("current",requestParam.getCurrent());
        map.put("size",requestParam.getSize());
        String retStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recyclebin/page",map);
        return JSON.parseObject(retStr,new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>(){
        });
    };

    /**
     * 恢复短链接
     * @param requestParam RB
     */
    default void recoverShortLink(RecycleBinRecoverReqDTO requestParam){
        try {
            HttpResponse response = HttpRequest.post("http://127.0.0.1:8001/api/short-link/v1/recyclebin/recover")
                    .body(JSON.toJSONString(requestParam))
                    .execute();

            // 解析响应
            Map<String, Object> result = JSON.parseObject(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {}
            );
            // 检查是否成功
            if (result != null && Boolean.FALSE.equals(result.get("success"))) {
                String errorMsg = (String) result.getOrDefault("message", "删除短链接失败");
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            throw new RuntimeException("操作失败: " + e.getMessage());
        }
    };

    default void deleteShortLink(RecycleBinDeleteReqDTO requestParam){
        try {
            HttpResponse response = HttpRequest.post("http://127.0.0.1:8001/api/short-link/v1/recyclebin/delete")
                    .body(JSON.toJSONString(requestParam))
                    .execute();

            // 解析响应
            Map<String, Object> result = JSON.parseObject(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {}
            );
            // 检查是否成功
            if (result != null && Boolean.FALSE.equals(result.get("success"))) {
                String errorMsg = (String) result.getOrDefault("message", "删除短链接失败");
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            throw new RuntimeException("操作失败: " + e.getMessage());
        }
    };

    /**
     * 访问单个短链接指定时间内监控数据
     *
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问分组短链接指定时间内监控数据
     *
     * @param requestParam 访分组问短链接监控请求参数
     * @return 分组短链接监控信息
     */
    default Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam){
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/access-record",stringObjectMap);
        return JSON.parseObject(resultBodyStr,new TypeReference<>(){});
    };

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam){
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group",stringObjectMap);
        return JSON.parseObject(resultBodyStr,new TypeReference<>(){});
    };

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 短链接批量创建响应
     */
    default Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create/batch", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }
}
