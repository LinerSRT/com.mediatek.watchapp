package android.support.v4.util;

import java.util.LinkedHashMap;

public class LruCache<K, V> {
    private int createCount;
    private int evictionCount;
    private int hitCount;
    private final LinkedHashMap<K, V> map;
    private int maxSize;
    private int missCount;
    private int size;

    public LruCache(int maxSize) {
        if (maxSize > 0) {
            this.maxSize = maxSize;
            this.map = new LinkedHashMap(0, 0.75f, true);
            return;
        }
        throw new IllegalArgumentException("maxSize <= 0");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V get(K r5) {
        /*
        r4 = this;
        r3 = 0;
        if (r5 == 0) goto L_0x003a;
    L_0x0003:
        monitor-enter(r4);
        r2 = r4.map;	 Catch:{ all -> 0x004b }
        r1 = r2.get(r5);	 Catch:{ all -> 0x004b }
        if (r1 != 0) goto L_0x0043;
    L_0x000c:
        r2 = r4.missCount;	 Catch:{ all -> 0x004b }
        r2 = r2 + 1;
        r4.missCount = r2;	 Catch:{ all -> 0x004b }
        monitor-exit(r4);	 Catch:{ all -> 0x004b }
        r0 = r4.create(r5);
        if (r0 == 0) goto L_0x004e;
    L_0x0019:
        monitor-enter(r4);
        r2 = r4.createCount;	 Catch:{ all -> 0x0055 }
        r2 = r2 + 1;
        r4.createCount = r2;	 Catch:{ all -> 0x0055 }
        r2 = r4.map;	 Catch:{ all -> 0x0055 }
        r1 = r2.put(r5, r0);	 Catch:{ all -> 0x0055 }
        if (r1 != 0) goto L_0x004f;
    L_0x0028:
        r2 = r4.size;	 Catch:{ all -> 0x0055 }
        r3 = r4.safeSizeOf(r5, r0);	 Catch:{ all -> 0x0055 }
        r2 = r2 + r3;
        r4.size = r2;	 Catch:{ all -> 0x0055 }
    L_0x0031:
        monitor-exit(r4);	 Catch:{ all -> 0x0055 }
        if (r1 != 0) goto L_0x0058;
    L_0x0034:
        r2 = r4.maxSize;
        r4.trimToSize(r2);
        return r0;
    L_0x003a:
        r2 = new java.lang.NullPointerException;
        r3 = "key == null";
        r2.<init>(r3);
        throw r2;
    L_0x0043:
        r2 = r4.hitCount;	 Catch:{ all -> 0x004b }
        r2 = r2 + 1;
        r4.hitCount = r2;	 Catch:{ all -> 0x004b }
        monitor-exit(r4);	 Catch:{ all -> 0x004b }
        return r1;
    L_0x004b:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x004b }
        throw r2;
    L_0x004e:
        return r3;
    L_0x004f:
        r2 = r4.map;	 Catch:{ all -> 0x0055 }
        r2.put(r5, r1);	 Catch:{ all -> 0x0055 }
        goto L_0x0031;
    L_0x0055:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0055 }
        throw r2;
    L_0x0058:
        r2 = 0;
        r4.entryRemoved(r2, r5, r0, r1);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.get(java.lang.Object):V");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void trimToSize(int r7) {
        /*
        r6 = this;
    L_0x0000:
        monitor-enter(r6);
        r3 = r6.size;	 Catch:{ all -> 0x0027 }
        if (r3 >= 0) goto L_0x002a;
    L_0x0005:
        r3 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0027 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0027 }
        r4.<init>();	 Catch:{ all -> 0x0027 }
        r5 = r6.getClass();	 Catch:{ all -> 0x0027 }
        r5 = r5.getName();	 Catch:{ all -> 0x0027 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x0027 }
        r5 = ".sizeOf() is reporting inconsistent results!";
        r4 = r4.append(r5);	 Catch:{ all -> 0x0027 }
        r4 = r4.toString();	 Catch:{ all -> 0x0027 }
        r3.<init>(r4);	 Catch:{ all -> 0x0027 }
        throw r3;	 Catch:{ all -> 0x0027 }
    L_0x0027:
        r3 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0027 }
        throw r3;
    L_0x002a:
        r3 = r6.map;	 Catch:{ all -> 0x0027 }
        r3 = r3.isEmpty();	 Catch:{ all -> 0x0027 }
        if (r3 != 0) goto L_0x0038;
    L_0x0032:
        r3 = r6.size;	 Catch:{ all -> 0x0027 }
        if (r3 > r7) goto L_0x003d;
    L_0x0036:
        monitor-exit(r6);	 Catch:{ all -> 0x0027 }
        return;
    L_0x0038:
        r3 = r6.size;	 Catch:{ all -> 0x0027 }
        if (r3 != 0) goto L_0x0005;
    L_0x003c:
        goto L_0x0032;
    L_0x003d:
        r3 = r6.map;	 Catch:{ all -> 0x0027 }
        r3 = r3.isEmpty();	 Catch:{ all -> 0x0027 }
        if (r3 != 0) goto L_0x0036;
    L_0x0045:
        r3 = r6.map;	 Catch:{ all -> 0x0027 }
        r3 = r3.entrySet();	 Catch:{ all -> 0x0027 }
        r3 = r3.iterator();	 Catch:{ all -> 0x0027 }
        r1 = r3.next();	 Catch:{ all -> 0x0027 }
        r1 = (java.util.Map.Entry) r1;	 Catch:{ all -> 0x0027 }
        r0 = r1.getKey();	 Catch:{ all -> 0x0027 }
        r2 = r1.getValue();	 Catch:{ all -> 0x0027 }
        r3 = r6.map;	 Catch:{ all -> 0x0027 }
        r3.remove(r0);	 Catch:{ all -> 0x0027 }
        r3 = r6.size;	 Catch:{ all -> 0x0027 }
        r4 = r6.safeSizeOf(r0, r2);	 Catch:{ all -> 0x0027 }
        r3 = r3 - r4;
        r6.size = r3;	 Catch:{ all -> 0x0027 }
        r3 = r6.evictionCount;	 Catch:{ all -> 0x0027 }
        r3 = r3 + 1;
        r6.evictionCount = r3;	 Catch:{ all -> 0x0027 }
        monitor-exit(r6);	 Catch:{ all -> 0x0027 }
        r3 = 1;
        r4 = 0;
        r6.entryRemoved(r3, r0, r2, r4);
        goto L_0x0000;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.util.LruCache.trimToSize(int):void");
    }

    protected void entryRemoved(boolean evicted, K k, V v, V v2) {
    }

    protected V create(K k) {
        return null;
    }

    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result >= 0) {
            return result;
        }
        throw new IllegalStateException("Negative size: " + key + "=" + value);
    }

    protected int sizeOf(K k, V v) {
        return 1;
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    public final synchronized String toString() {
        String format;
        int hitPercent = 0;
        synchronized (this) {
            int accesses = this.hitCount + this.missCount;
            if (accesses != 0) {
                hitPercent = (this.hitCount * 100) / accesses;
            }
            format = String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", new Object[]{Integer.valueOf(this.maxSize), Integer.valueOf(this.hitCount), Integer.valueOf(this.missCount), Integer.valueOf(hitPercent)});
        }
        return format;
    }
}
