package cn.jason31416.authX.util;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 带有定时过期功能的Map接口
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface TimedMap<K, V> extends Map<K, V> {

    /**
     * 设置全局过期时间
     * @param duration 时间长度
     * @param unit 时间单位
     */
    void setExpirationTime(long duration, TimeUnit unit);

    /**
     * 获取全局过期时间（毫秒）
     * @return 过期时间（毫秒）
     */
    long getExpirationTime();

    /**
     * 获取指定键的剩余生存时间
     * @param key 键
     * @param unit 时间单位
     * @return 剩余生存时间，如果键不存在或已过期返回-1
     */
    long getTimeToLive(K key, TimeUnit unit);

    /**
     * 获取指定键的最后更新时间
     * @param key 键
     * @return 最后更新时间（毫秒时间戳），如果键不存在返回-1
     */
    long getLastUpdateTime(K key);

    /**
     * 手动移除所有过期条目
     */
    void removeExpiredEntries();

    /**
     * 设置指定键的过期时间（覆盖全局设置）
     * @param key 键
     * @param duration 时间长度
     * @param unit 时间单位
     * @return 是否设置成功（键存在时返回true）
     */
    boolean setExpirationTime(K key, long duration, TimeUnit unit);

    /**
     * 获取指定键的过期时间（毫秒）
     * @param key 键
     * @return 过期时间（毫秒），如果使用全局设置返回-1
     */
    long getExpirationTime(K key);

    /**
     * 重置指定键的过期时间（使用最后更新时间重新计算）
     * @param key 键
     * @return 是否重置成功（键存在时返回true）
     */
    boolean renewKey(K key);

    /**
     * 关闭清理任务
     */
    void shutdown();
}
