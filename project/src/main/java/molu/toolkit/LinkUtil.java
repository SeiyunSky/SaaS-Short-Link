package molu.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
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

    /**
     * 获取用户访问设备
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }


    /**
     * 获取用户访问网络类型
     * @param request HTTP请求对象
     * @return 网络类型（Wi-Fi/4G/5G/Unknown）
     */
    public static String getNetwork(HttpServletRequest request) {
        // 1. 优先从Header中获取网络信息（移动端常用）
        String networkHeader = request.getHeader("X-Network-Type");
        if (networkHeader != null) {
            return normalizeNetworkType(networkHeader);
        }

        // 2. 通过User-Agent分析（适用于部分场景）
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.toLowerCase().contains("wifi")) {
            return "WiFi";
        }

        // 3. 通过IP特征判断（需要IP库支持）
        String ip = request.getRemoteAddr();
        if (isMobileNetworkIP(ip)) {
            return "Mobile";
        }

        // 默认返回未知
        return "Unknown";
    }

    private static String normalizeNetworkType(String rawType) {
        if (rawType == null) return "Unknown";

        String lowerType = rawType.toLowerCase();
        if (lowerType.contains("wifi")) {
            return "WiFi";
        } else if (lowerType.contains("5g")) {
            return "5G";
        } else if (lowerType.contains("4g")) {
            return "4G";
        } else if (lowerType.contains("3g")) {
            return "3G";
        } else if (lowerType.contains("mobile")) {
            return "Mobile";
        }
        return rawType;
    }

    private static boolean isMobileNetworkIP(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.");
    }

    /**
     * 获取原始链接中的域名
     * 如果原始链接包含 www 开头的话需要去掉
     *
     * @param url 创建或者修改短链接的原始链接
     * @return 原始链接中的域名
     */
    public static String extractDomain(String url) {
        String domain = null;
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (StrUtil.isNotBlank(host)) {
                domain = host;
                if (domain.startsWith("www.")) {
                    domain = host.substring(4);
                }
            }
        } catch (Exception ignored) {
        }
        return domain;
    }
}
