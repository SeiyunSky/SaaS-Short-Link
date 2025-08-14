package molu.mq.idempotent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
//通过简单判断是否有幂等标识来做幂等
public class MessageQueueIdempotentHandler {
    private final StringRedisTemplate stringRedisTemplate;

    //幂等标识
    private static final String IDEMPOTENT_KEY_PREFIX = "short-link:idempotent:";

    /**
     * 判断当前消息是否消费
     * @param messageId 唯一标识
     * @return 真假
     */
    public boolean isMessageProcessed(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "0",1, TimeUnit.MINUTES));
    }

    /**
     * 判断是否完成消费
     * @param messageId 标识
     * @return 真假
     */
    public boolean isAccomplish(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        return Objects.equals(stringRedisTemplate.opsForValue().get(key),"1");
    }

    /**
     * 设置完成状态
     * @param messageId 标识
     */
    public void setAccomplish(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.opsForValue().set(key,"1",1, TimeUnit.MINUTES);
    }

    /**
     * 消息消费失败，删除幂等标识
     * @param messageId 唯一标识
     */
    public void deleteMessageProcessed(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.delete(key);
    }
}
