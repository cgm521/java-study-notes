# 类声明、常量、成员变量
```
public class HashMap<K, V> extends AbstractMap<K, V>
        implements Map<K, V>, Cloneable, Serializable
----------------
//序列化ID
    private static final long serialVersionUID = 362498820763181265L;
    //哈希桶数组的默认容量
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    //网上很多文章都说这个值是哈希桶数组能够达到的最大容量，其实这样说并不准确
    //从 resize() 方法的扩容机制可以看出来，HashMap 每次扩容都是将数组的现有容量增大一倍
    //如果现有容量已大于或等于 MAXIMUM_CAPACITY ，则不允许再次扩容
    //否则即使此次扩容会导致容量超出 MAXIMUM_CAPACITY ，那也是允许的
    static final int MAXIMUM_CAPACITY = 1 << 30;
-----------
    //装载因子的默认值
    //装载因子用于规定数组在自动扩容之前可以数据占有其容量的最高比例，即当数据量占有数组的容量达到这个比例后，数组将自动扩容
    //装载因子衡量的是一个散列表的空间的使用程度，负载因子越大表示散列表的装填程度越高，反之愈小
    //对于使用链表的散列表来说，查找一个元素的平均时间是O(1+a)，因此如果负载因子越大，则对空间的利用程度更高，相对应的是查找效率的降低
    //如果负载因子太小，那么数组的数据将过于稀疏，对空间的利用率低
    //官方默认的负载因子为0.75，是平衡空间利用率和运行效率两者之后的结果
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    //为了提高效率，当链表的长度超出这个值时，就将链表转换为红黑树
    static final int TREEIFY_THRESHOLD = 8;

    //哈希桶数组，在第一次使用时才初始化
    //容量值应是2的整数倍
    transient Node<K, V>[] table;
    /**
     * Holds cached entrySet(). Note that AbstractMap fields are used
     * for keySet() and values().
     */
    transient Set<Map.Entry<K, V>> entrySet;
    //Map的大小
    transient int size;
    //每当Map的结构发生变化时，此参数就会递增
    //当在对Map进行迭代操作时，迭代器会检查此参数值
    //如果检查到此参数的值发生变化，就说明在迭代的过程中Map的结构发生了变化，因此会直接抛出异常
    transient int modCount;
    //数组的扩容临界点，当数组的数据量达到这个值时就会进行扩容操作
    //计算方法：当前容量 x 装载因子
    int threshold;
    //使用的装载因子值
    final float loadFactor;
```
# 哈希算法
```
//计算哈希值
    static final int hash(Object key) {
        int h;
        //高位参与运算
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    //根据 key 值获取 Value
    public V get(Object key) {
        Node<K, V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }
    //查找指定结点
    final Node<K, V> getNode(int hash, Object key) {
        ···
        //只有当 table 不为空且 hash 对应的位置不为 null 才有可获取的元素值
        if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) {
           ···
        }
        return null;
    }
```
**确定键值对在哈希桶数组的位置的步骤分为三步：计算 key 的 hashCode（h = key.hashCode()）、高位运算（h >>> 16）、取模运算（(n - 1) & hash）**
# put
```
//插入数据
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
    //计算哈希值
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    /**
     * Implements Map.put and related methods
     *
     * @param hash         hash for key
     * @param key          the key
     * @param value        the value to put
     * @param onlyIfAbsent 为 true 表示不会覆盖有相同 key 的非 null value，否则会覆盖原有值
     * @param evict        if false, the table is in creation mode.
     * @return previous value, or null if none
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        Node<K, V>[] tab;
        Node<K, V> p;
        int n, i;
        //如果 table 还未初始化，则调用 resize 方法进行初始化
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        //判断要存入的 key 是否存在哈希冲突，等于 null 说明不存在冲突
        if ((p = tab[i = (n - 1) & hash]) == null)
            //直接在索引 i 处构建包含待存入元素的结点
            tab[i] = newNode(hash, key, value, null);
        else { //走入本分支，说明待存入的 key 存在哈希冲突
            Node<K, V> e;
            K k;
            //p 值已在上一个 if 语句中赋值了，此处就直接来判断 key 值之间的相等性
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                //指向冲突的头结点
                e = p;
            //如果头结点的 key 与待插入的 key 不相等，且头结点是 TreeNode 类型，说明该 hash 值是采用红黑树来处理冲突
            else if (p instanceof TreeNode)
                //如果红黑数中包含有相同 key 的结点，则返回该结点，否则返回 null
                e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
            else { //采用链表来处理 hash 值冲突
                for (int binCount = 0; ; ++binCount) {
                    //当遍历到链表尾部时
                    if ((e = p.next) == null) {
                        //构建一个新的结点添加到链表尾部
                        p.next = newNode(hash, key, value, null);
                        //如果链表的长度已达到允许的最大长度 TREEIFY_THRESHOLD - 1 时，就将链表转换为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    //当 e 指向的结点的 key 值与待插入的 key 相等时则跳出循环
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            //如果 e != null，说明原先已存在相同 key 的键
            if (e != null) {
                V oldValue = e.value;
                //只有当 onlyIfAbsent 为 true 且 oldValue 不为 null 时才不会覆盖原有值
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                //用于 LinkedHashMap ，在 HashMap 中是空实现
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        //当元素数量达到扩容临界点时，需要进行扩容
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```




# get
```
//根据 key 值获取 Value
    public V get(Object key) {
        Node<K, V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }
    //查找指定结点
    final Node<K, V> getNode(int hash, Object key) {
        Node<K, V>[] tab;
        Node<K, V> first, e;
        int n;
        K k;
        //只有当 table 不为空且 hash 对应的位置不为 null 才有可获取的元素值
        if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) {
            //如果头结点的 hash 值与 Key 与待插入数据相等的话，则说明找到了对应值
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // != null 说明存在哈希冲突
            if ((e = first.next) != null) {
                //如果是由红黑树来处理哈希冲突，则由此查找相应结点
                if (first instanceof TreeNode)
                    return ((TreeNode<K, V>) first).getTreeNode(hash, key);
                //遍历链表
                do {
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```
# remove
```
public V remove(Object key) {
        Node<K, V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
                null : e.value;
    }
    /**
     * Implements Map.remove and related methods
     *
     * @param hash       key 的哈希值
     * @param key        the key
     * @param value      key对应的值，只有当 matchValue 为 true 时才需要使用到，否则忽略该值
     * @param matchValue 如果为 true ，则只有当 Map 中存在某个键 equals key 且 value 相等时才会移除该元素，否则只要 key 的 hash 值相等就直接移除该元素
     * @param movable    if false do not move other nodes while removing
     * @return the node, or null if none
     */
    final Node<K, V> removeNode(int hash, Object key, Object value,
                                boolean matchValue, boolean movable) {
        Node<K, V>[] tab;
        Node<K, V> p;
        int n, index;
        //只有当 table 不为空且 hash 对应的索引位置存在值时才有可移除的对象
        if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = (n - 1) & hash]) != null) {
            Node<K, V> node = null, e;
            K k;
            V v;
            //如果与头结点的 key 相等
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;
            else if ((e = p.next) != null) { //存在哈希冲突
                //用红黑树来处理哈希冲突
                if (p instanceof TreeNode)
                    //查找对应结点
                    node = ((TreeNode<K, V>) p).getTreeNode(hash, key);
                else { //用链表来处理哈希冲突
                    do {
                        if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            //node != null 说明存在相应结点
            //如果 matchValue 为 false ，则通过之前的判断可知查找到的结点的 key 与 参数 key 的哈希值一定相等，此处就可以直接移除结点 node
            //如果 matchValue 为 true ，则当 value 相等时才需要移除该结点
            if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))) {
                if (node instanceof TreeNode) //对应红黑树
                    ((TreeNode<K, V>) node).removeTreeNode(this, tab, movable);
                else if (node == p) //对应 key 与头结点相等的情况，此时直接将指针移向下一位即可
                    tab[index] = node.next;
                else //对应的是链表的情况
                    p.next = node.next;
                ++modCount;
                --size;
                //用于 LinkedHashMap ，在 HashMap 中是空实现
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }
```
# resize()
```
//用于初始化 table 或者对之进行扩容
    //并返回新的数组
    final Node<K, V>[] resize() {
        //扩容前的数组
        Node<K, V>[] oldTab = table;
        //扩容前数组的容量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        //扩容前Map的扩容临界值
        int oldThr = threshold;
        //扩容后数组的容量和扩容临界值
        int newCap, newThr = 0;
        if (oldCap > 0) {
            //oldCap > 0 对应的是 table 已被初始化的情况，此时是来判断是否需要进行扩容
            //如果数组已达到最大容量，则不再进行扩容，并将扩容临界点 threshold 提升到 Integer.MAX_VALUE，结束
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY) {
                //如果将数组的现有容量提升到现在的两倍依然小于允许的最大容量，而且现有容量大于或等于默认容量
                //则将数组的容量和扩容临界值均提升为原先的两倍
                newThr = oldThr << 1;
            }
            //此处应该还有一种情况
            //即将数组的现有容量提升到现在的两倍后大于 MAXIMUM_CAPACITY 的情况
            //此时 newThr 等于 0，newCap 等于 oldCap 的两倍值
            //此处并没有对 newCap 的数值进行还原，说明 HashMap 是允许扩容后容量超出 MAXIMUM_CAPACITY 的
            //只是在现有容量超出 MAXIMUM_CAPACITY 后，不允许再次进行扩容
        } else if (oldThr > 0) {
            //oldCap <= 0 && oldThr > 0 对应的是 table 还未被初始化，且在调用构造函数时有传入初始化大小 initialCapacity 或者包含原始数据的 Map 的情况
            //这导致了 threshold 被赋值 (tableSizeFor 方法)
            //此时就直接将Map的容量提升为 threshold，在后边重新计算新的扩容临界值
            newCap = oldThr;
        } else {
            //oldCap <= 0 && oldThr <= 0 对应的是 table 还未被初始化，且调用的是无参数的构造函数
            //此时就将 table 的容量扩充到默认值大小，并使用默认的装载因子值来计算扩容临界值
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            //计算扩容后新的扩容临界值
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes", "unchecked"})
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        table = newTab;
        //如果旧数组中存在值，则需要将其中的数据复制到新数组中
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K, V> e;
                if ((e = oldTab[j]) != null) {
                    //将旧数组中的引用切换，帮助GC回收
                    oldTab[j] = null;
                    //e.next == null 说明元素 e 没有产生 hash 冲突，因此可以直接转移该元素
                    if (e.next == null)
                        //计算元素 e 在新数组中的位置
                        newTab[e.hash & (newCap - 1)] = e;
                    //e instanceof TreeNode 说明元素 e 有产生 hash 冲突，且使用红黑树管理冲突的元素
                    else if (e instanceof TreeNode)
                        ((TreeNode<K, V>) e).split(this, newTab, j, oldCap);
                    //走入如下分支，说明元素 e 有产生 hash 冲突，且使用链表结构来管理冲突的元素
                    else { // 链表优化重hash的代码块
                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        do {
                            next = e.next;
                            // 原索引
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            } else {// 原索引+oldCap
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 原索引放到bucket里
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                         // 原索引+oldCap放到bucket里
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```


