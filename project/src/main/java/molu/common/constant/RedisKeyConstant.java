package molu.common.constant;

/**
 * Redis Key常量类
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转前缀 key
     */
    public static final String GOTO_KEY = "short-link_goto_%s";

    /**
     * 短链接空值跳转前缀 key
     */
    public static final String GOTO_IS_NULL_KEY = "short-link_is_null_goto_%s";


    /**
     * 短链接跳转锁前缀 key
     */
    public static final String LOCK_GOTO_KEY = "short-link_lock_goto_%s";
}
