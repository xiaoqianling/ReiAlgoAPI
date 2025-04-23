package com.rei.algo.util;

import cn.hutool.core.util.IdUtil;
import org.apache.commons.lang3.RandomStringUtils;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public class IDGenerator {
    public static void main(String[] args) {
        System.out.println(generateAlphanumericId());
    }

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int USER_ID_LENGTH = 8;
    private static final int OTHER_ID_LENGTH = 16;
    private static final long MIN_USER_ID = 10_000_000L; // 10000000
    private static final long MAX_USER_ID = 99_999_999L; // 99999999

    // 雪花id
    public static long generateSnowflakeId() {
        return IdUtil.getSnowflakeNextId();
    }

    // 24 字符
    public static String generateObjectId() {
        return IdUtil.objectId();
    }

    // 无连字符的b17f24ff026d40949c85a24f4f375d42
    public static String generateSimpleUUID() {
        return IdUtil.simpleUUID();
    }

    // 8位随机数
    public static int generate8DigitId() {
        return 10000000 + secureRandom.nextInt(90000000);
    }

    // 9位随机数
    public static int generate9DigitId() {
        return 100_000_000 + secureRandom.nextInt(900_000_000);
    }

    /**
     * 生成 8 位无前导零的数字用户 ID (字符串形式)。
     * 注意：此方法不保证全局唯一性，需要在 Service 层结合数据库检查。
     *
     * @return 8位数字ID字符串
     */
    public static String generateUserId() {
        // 使用 ThreadLocalRandom 生成指定范围内的 long
        long randomId = ThreadLocalRandom.current().nextLong(MIN_USER_ID, MAX_USER_ID + 1);
        return String.valueOf(randomId);
    }

    /**
     * 生成指定长度的字母数字混合 ID (例如：算法、帖子、标签、评论 ID)。
     * 使用 Apache Commons Lang3 库。
     *
     * @return 16位字母数字ID字符串
     */
    public static String generateAlphanumericId() {
        return RandomStringUtils.random(OTHER_ID_LENGTH, true, true);
    }

    /**
     * 生成指定长度的字母数字混合 ID。
     * 使用 Apache Commons Lang3 库。
     *
     * @param length ID长度
     * @return 指定长度的字母数字ID字符串
     */
    public static String generateAlphanumericId(int length) {
        return RandomStringUtils.random(length, true, true);
    }

    // 如果需要更高安全性的随机数，可以使用 SecureRandom，但性能稍低
    // public static String generateSecureAlphanumericId() {
    //     byte[] randomBytes = new byte[OTHER_ID_LENGTH];
    //     secureRandom.nextBytes(randomBytes);
    //     // 这里需要将 byte 数组转换为字母数字字符串，例如 Base64 或自定义映射
    //     // return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).substring(0, OTHER_ID_LENGTH);
    //     // 或者使用 RandomStringUtils 配合 SecureRandom
    //     return RandomStringUtils.random(OTHER_ID_LENGTH, 0, 0, true, true, null, secureRandom);
    // }
}