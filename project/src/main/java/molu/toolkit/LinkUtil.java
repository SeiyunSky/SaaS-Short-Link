package molu.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import lombok.Data;

import java.util.Date;
import java.util.Optional;

import static molu.common.constant.ShortUrlConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短链接缓存有效期时间
     * @param validDate time
     * @return 有效期时间戳
     */
    public static long getLinkCacheValidDate(Date validDate) {
        return Optional.ofNullable(validDate)
                .map(each -> {
                    long ttl = DateUtil.between(new Date(), each, DateUnit.MS);
                    return Math.max(0, ttl); // 确保TTL非负
                })
                .orElse(DEFAULT_CACHE_VALID_TIME );
    }
}
