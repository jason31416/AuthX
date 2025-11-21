package cn.jason31416.authX.util;

import javax.annotation.Nonnull;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class TimedHashMap<K, V> extends ConcurrentHashMap<K, V> implements TimedMap<K, V> {
    private final ConcurrentMap<K, Long> timestamps = new ConcurrentHashMap<>();
    private final ConcurrentMap<K, Long> keyExpirationTimes = new ConcurrentHashMap<>();
    private final AtomicLong globalExpirationTime = new AtomicLong(60000);
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public TimedHashMap(long expirationTime, TimeUnit unit) {
        super();
        this.globalExpirationTime.set(unit.toMillis(expirationTime));
        scheduleCleanupTask();
    }

    public TimedHashMap() {
        this(60, TimeUnit.SECONDS);
    }

    public TimedHashMap(int initialCapacity, long expirationTime, TimeUnit unit) {
        super(initialCapacity);
        this.globalExpirationTime.set(unit.toMillis(expirationTime));
        scheduleCleanupTask();
    }

    public TimedHashMap(Map<? extends K, ? extends V> m, long expirationTime, TimeUnit unit) {
        super(m);
        this.globalExpirationTime.set(unit.toMillis(expirationTime));
        scheduleCleanupTask();
    }

    public TimedHashMap(int initialCapacity, float loadFactor, long expirationTime, TimeUnit unit) {
        super(initialCapacity, loadFactor);
        this.globalExpirationTime.set(unit.toMillis(expirationTime));
        scheduleCleanupTask();
    }

    public TimedHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, long expirationTime, TimeUnit unit) {
        super(initialCapacity, loadFactor, concurrencyLevel);
        this.globalExpirationTime.set(unit.toMillis(expirationTime));
        scheduleCleanupTask();
    }

    private void scheduleCleanupTask() {
        // 每分钟执行一次清理任务
        cleanupScheduler.scheduleAtFixedRate(this::removeExpiredEntries,
                1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void setExpirationTime(long duration, TimeUnit unit) {
        this.globalExpirationTime.set(unit.toMillis(duration));
    }

    @Override
    public long getExpirationTime() {
        return globalExpirationTime.get();
    }

    @Override
    public boolean setExpirationTime(K key, long duration, TimeUnit unit) {
        if (!containsKey(key)) {
            return false;
        }
        keyExpirationTimes.put(key, unit.toMillis(duration));
        return true;
    }

    @Override
    public long getExpirationTime(K key) {
        return keyExpirationTimes.getOrDefault(key, -1L);
    }

    @Override
    public boolean renewKey(K key) {
        if (!containsKey(key)) {
            return false;
        }
        updateTimestamp(key);
        return true;
    }

    private void updateTimestamp(K key) {
        timestamps.put(key, System.currentTimeMillis());
    }

    private void removeTimestamp(K key) {
        timestamps.remove(key);
        keyExpirationTimes.remove(key);
    }

    private long getExpirationTimeForKey(K key) {
        return keyExpirationTimes.getOrDefault(key, globalExpirationTime.get());
    }

    private boolean isExpired(K key) {
        Long timestamp = timestamps.get(key);
        if (timestamp == null) {
            return true;
        }
        long expirationTime = getExpirationTimeForKey(key);
        return System.currentTimeMillis() - timestamp > expirationTime;
    }

    @Override
    public void removeExpiredEntries() {
        try {
            for (K key : timestamps.keySet()) {
                if (isExpired(key)) {
                    remove(key);
                }
            }
        } catch (ConcurrentModificationException ignored) {}
    }

    @Override
    public long getTimeToLive(K key, TimeUnit unit) {
        Long timestamp = timestamps.get(key);
        if (timestamp == null) {
            return -1;
        }

        long expirationTime = getExpirationTimeForKey(key);
        long elapsed = System.currentTimeMillis() - timestamp;
        long ttl = expirationTime - elapsed;

        if (ttl <= 0) {
            return 0;
        }

        return unit.convert(ttl, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getLastUpdateTime(K key) {
        Long timestamp = timestamps.get(key);
        return timestamp != null ? timestamp : -1;
    }

    @Override
    public V put(@Nonnull K key, @Nonnull V value) {
        V result = super.put(key, value);
        updateTimestamp(key);
        return result;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        V result = super.putIfAbsent(key, value);
        if (result == null) {
            updateTimestamp(key);
        }
        return result;
    }

    @Override
    public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
        super.putAll(m);
        for (K key : m.keySet()) {
            updateTimestamp(key);
        }
    }

    @Override
    public V replace(K key, V value) {
        V result = super.replace(key, value);
        if (result != null) {
            updateTimestamp(key);
        }
        return result;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        boolean result = super.replace(key, oldValue, newValue);
        if (result) {
            updateTimestamp(key);
        }
        return result;
    }

    @Override
    public V compute(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V result = super.compute(key, remappingFunction);
        if (result != null) {
            updateTimestamp(key);
        }
        return result;
    }

    @Override
    public V computeIfAbsent(K key, @Nonnull Function<? super K, ? extends V> mappingFunction) {
        V result = super.computeIfAbsent(key, mappingFunction);
        if (result != null) {
            updateTimestamp(key);
        }
        return result;
    }

    @Override
    public V computeIfPresent(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V result = super.computeIfPresent(key, remappingFunction);
        if (result != null) {
            updateTimestamp(key);
        }
        return result;
    }

    @Override
    public V merge(K key, @Nonnull V value, @Nonnull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        V result = super.merge(key, value, remappingFunction);
        if (result != null) {
            updateTimestamp(key);
        }
        return result;
    }

    @Override
    public V remove(@Nonnull Object key) {
        V result = super.remove(key);
        if (result != null) {
            removeTimestamp((K) key);
        }
        return result;
    }

    @Override
    public boolean remove(Object key, Object value) {
        boolean result = super.remove(key, value);
        if (result) {
            removeTimestamp((K) key);
        }
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        timestamps.clear();
        keyExpirationTimes.clear();
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value != null && isExpired((K) key)) {
            remove(key);
            return null;
        }
        return value;
    }

    @Override
    public boolean containsKey(Object key) {
        if (super.containsKey(key)) {
            if (isExpired((K) key)) {
                remove(key);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        removeExpiredEntries();
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        removeExpiredEntries();
        return super.isEmpty();
    }

    @Override
    public @Nonnull Set<Entry<K, V>> entrySet() {
        removeExpiredEntries();
        return super.entrySet();
    }

    @Override
    public @Nonnull KeySetView<K, V> keySet() {
        removeExpiredEntries();
        return super.keySet();
    }

    @Override
    public void shutdown() {
        cleanupScheduler.shutdown();
    }
}