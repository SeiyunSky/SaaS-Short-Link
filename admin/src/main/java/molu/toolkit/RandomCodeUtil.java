package molu.toolkit;

import java.util.Random;

/**
 * 分组ID随机生成器
 */
public final class RandomCodeUtil {

    // 定义可选的字符集（数字+大小写字母）
    private static final String CHAR_SET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Random random = new Random();

    public static String generateRandomCode() {
        return generateRandomCode(6);
    }

    /**
     * 生成6位随机码（数字+字母）
     */
    public static String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 从CHAR_SET中随机选取一个字符
            int index = random.nextInt(CHAR_SET.length());
            sb.append(CHAR_SET.charAt(index));
        }
        return sb.toString();
    }
}