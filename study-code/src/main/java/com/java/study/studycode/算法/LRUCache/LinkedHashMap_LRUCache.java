package com.java.study.studycode.算法.LRUCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author cgm
 * @version $Id: LinkedHashMap_LRUCache.java, v 0.1 2018-06-13 11:36 cgm Exp $$
 *          基于LinkedHashMap实现的LRU缓存
 */
public class LinkedHashMap_LRUCache<K, V> {
    private static final float loadFactor = 0.75f;
    private int cacheSize;

    private LinkedHashMap<K, V> map;

    public LinkedHashMap_LRUCache(int cacheSize) {
        this.cacheSize = cacheSize;
        int initialCapacity = (int) Math.ceil(cacheSize / loadFactor) + 1;
        map = new LinkedHashMap<K, V>(initialCapacity, loadFactor, true) {
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
                return size() > cacheSize;
            }
        };
        this.cacheSize = cacheSize;
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized int usedEntries() {
        return map.size();
    }

    public synchronized Collection<Map.Entry<K, V>> getAll() {
        return new ArrayList<Map.Entry<K, V>>(map.entrySet());
    }

    @Override
    public String toString() {
        return "LinkedHashMap_LRUCache{" +
                "cacheSize=" + cacheSize +
                ", map=" + map +
                '}';
    }

    public static void main(String[] args) {
        LinkedHashMap_LRUCache<String, String> cache = new LinkedHashMap_LRUCache<String, String>(3);
        cache.put("1", "one");
        cache.put("2", "two");
        cache.put("3", "three");
        cache.put("4", "fore");
        cache.get("2");
        System.out.println(cache);
    }

}
