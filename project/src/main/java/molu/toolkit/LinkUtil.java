package molu.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * 获取请求的 IP 地址
     * @param request 请求
     * @return 用户IP
     */
    public static String getIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }

    /**
     * 从User-Agent字符串中识别操作系统
     * @param userAgent 浏览器User-Agent
     * @return 操作系统名称
     */
    public static String getOs(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac os") || userAgent.contains("macos")) {
            return "Mac OS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad") || userAgent.contains("ios")) {
            return "iOS";
        } else if (userAgent.contains("unix")) {
            return "Unix";
        } else {
            return "Unknown";
        }
    }

    /**
     * 从HttpServletRequest中获取操作系统信息
     * @param request HttpServletRequest对象
     * @return 操作系统名称
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return getOs(userAgent);
    }

    /**
     * 获取用户访问浏览器
     *
     * @param request 请求
     * @return 访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.toLowerCase().contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.toLowerCase().contains("opera")) {
            return "Opera";
        } else if (userAgent.toLowerCase().contains("msie") || userAgent.toLowerCase().contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }
}
