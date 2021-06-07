package com.java.study.studycode.算法.LRUCache;

import java.util.Hashtable;

/**
 * @author cgm
 * @version $Id: DoubleLinkedAndHashTable_LRUCache.java, v 0.1 2018-06-13 10:29 cgm Exp $$
 * 双链表加hashTable
 */
public class DoubleLinkedAndHashTable_LRUCache<K, V> {
    /**
     * 缓存大小
     */
    private int cacheSize;
    /**
     * 当前链表大小
     */
    private int size;

    /**
     * 缓存容器
     */
    private Hashtable<K, Entry> nodes;
    /**
     * 表头
     */
    private Entry first;
    /**
     * 表尾
     */
    private Entry last;

    public DoubleLinkedAndHashTable_LRUCache(int cacheSize) {
        this.size = 0;
        this.cacheSize = cacheSize;
        this.nodes = new Hashtable<>(cacheSize);
    }

    /**
     * 获取缓存中对象,并把它放在最前面
     */
    public V get(K key) {
        Entry entry = nodes.get(key);
        if (entry != null) {
            moveToHead(entry);
            return (V) entry.value;
        }
        return null;
    }

    /**
     * 添加 entry到hashtable, 并把entry放在最前面
     */
    public void put(K key, V value) {
        Entry entry = nodes.get(key);
        if (entry == null) {
            if (size >= cacheSize) {
                removeLast();
            } else {
                size++;
            }
            entry = new Entry();
        }
        entry.key = key;
        entry.value = value;
        moveToHead(entry);
        nodes.put(key, entry);

    }

    /**
     * 将entry删除, 注意：删除操作只有在cache满了才会被执行
     */
    public void remove(K key) {
        Entry entry = nodes.get(key);
        if (entry != null) {
            if (entry.prev != null) {
                entry.prev.next = entry.next;
            }
            if (entry.next != null) {
                entry.next.prev = entry.prev;
            }
            if (last == entry) {
                last = entry.prev;
                last.next = null;
            }
            if (first == entry) {
                first = entry.next;
                first.prev = null;
            }
            nodes.remove(key);
        }

    }

    /**
     * 删除链表尾部节点，即使用最后 使用的entry
     */
    private void removeLast() {
        nodes.remove(last.key);
        if (last != null) {
            if (last.prev != null) {
                last.prev.next = null;
            } else {
                first = null;
            }
            last = last.prev;
        }
    }

    /**
     * 移动到链表头，表示这个节点是最新使用过的
     */
    private void moveToHead(Entry node) {
        if (first == node) {
            return;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }
        if (last == node) {
            last = node.prev;
        }
        if (first != null) {
            first.prev = node;
            node.next = first;
        }
        first = node;
        node.prev = null;
        if (last == null) {
            last = first;
        }
    }

    /**
     * 清空缓存
     */
    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Entry entry = nodes.get(first.key);
        while (entry != null) {
            sb.append(entry.key).append("->").append(entry.value).append("\n");
            entry = entry.next;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        DoubleLinkedAndHashTable_LRUCache cache = new DoubleLinkedAndHashTable_LRUCache(3);
        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        cache.put(4, "four");
        System.out.println(cache.nodes.compute(1, (k, v) -> k + "->" + v));
        System.out.println(cache.toString());
        cache.clear();

        String s = new String("a") + new String("b");
        String intern = s.intern();
        String b = "ab";

        System.out.println(s == b); // true

        String s1 = new String("c") + new String("d");
        String intern1 = s.intern();
        String b1 = "cd";

        System.out.println(s1 == b1); // false
    }

    static class Entry<K, V> {
        /**
         * 前一节点
         */
        Entry prev;
        /**
         * 后一节点
         */
        Entry next;
        /**
         * 键
         */
        K key;
        /**
         * 值
         */
        V value;
    }
}


