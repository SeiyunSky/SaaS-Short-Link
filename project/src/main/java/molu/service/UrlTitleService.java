package molu.service;


import molu.common.convention.result.Result;

/**
 * UrlTitle接口层
 */
public interface UrlTitleService  {

    /**
     * 根据url获取标题
     * @param url url
     * @return 标题
     */
    String getTitleByUrl(String url);
}
