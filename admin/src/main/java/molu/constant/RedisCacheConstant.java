package molu.constant;

/**
 * 短链接后管Redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 用户注册分布式锁
     */
    public static final String LOCK_USER_REGISTER = "shortlink:lock_user-register :";

    /**
     * 用户建组分布式锁
     */
    public static final String LOCK_GROUP_CREATE = "shortlink:lock_group_create :%s";

}
