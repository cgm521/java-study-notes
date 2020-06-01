# ThreadLocal

<a name="CmCTY"></a>
# 前奏
我们先看下面这段代码，是在多线程环境下使用ThreadLocl的例子
```java
public class ThreadLocalTest {

    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public static void main(String[] args) {
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                threadLocal.set(threadLocal.get()+1);
                System.out.println(Thread.currentThread().getName() + ":" + threadLocal.get());
            }, "Thread-" + i);
        }
        for (Thread thread : threads) {
            thread.start();
        }
    }
}
```
执行结果：<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590666144375-a38092ed-5529-4cdc-9295-793c2416770a.png#align=left&display=inline&height=227&margin=%5Bobject%20Object%5D&name=image.png&originHeight=227&originWidth=698&size=136986&status=done&style=none&width=698)<br />每个线程调用的都是同一个ThreadLocal对象，但是get的结果却看似和ThreadLocal无关，各个线程get出来的值像是相互隔离开的一样，这是为什么呢？<br />

<a name="u2kty"></a>
# 概述
ThreadLocal提供一个线程局部变量，访问某个变量的每个线程都能拥有自己的一个局部变量。ThreadLocal可以在多线程环境下保证局部变量的安全<br />ThreadLocal内部有个**内部类ThreadLocalMap**，ThreadLocalMap内部有个内部类Entry，Entry存储着一对键值对，key是ThreadLocal类型弱引用，value是Object类型数据。<br />先看一张图<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590667749212-7344172b-ff4f-47d2-8429-93280b0d3812.png#align=left&display=inline&height=496&margin=%5Bobject%20Object%5D&name=image.png&originHeight=496&originWidth=1011&size=120631&status=done&style=none&width=1011)<br />由上图可以看出，在Thread中持有一个ThreadLocalMap，ThreadLocalMap有属性table是个Entry数组，Entry的key是ThreadLocal类型，value是Object类型。也就是一个ThreadLocalMap可以持有多个ThreadLocal。也就是一个Thread包含一个ThreadLocalMap，包含多个ThreadLocal，**是Thread包含ThreadLocal**<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590667772709-5668146b-6782-4ef4-ab61-a4609fb2aabc.png#align=left&display=inline&height=481&margin=%5Bobject%20Object%5D&name=image.png&originHeight=481&originWidth=684&size=53729&status=done&style=none&width=684)
<a name="Y9xiu"></a>
# 源码解析
<a name="rQFE1"></a>
## set()
```java
public void set(T value) {
    // 获取当前线程
    Thread t = Thread.currentThread();
    // 获取当前线程的成员变量 thread.threadLocals
    ThreadLocalMap map = getMap(t);
    if (map != null)
        // 设置ThreadLocalMap的entry的值
        map.set(this, value);
    else
        createMap(t, value);
}
```
ThreadLocalMap中的Entry是以当前ThreadLocal对象为key，具体set分析如下
<a name="OscM7"></a>
### ThreadLocalMap#set(ThreadLocal<?> key, Object value)
```java
private void set(ThreadLocal<?> key, Object value) {
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);

    // 采用开放定址法解决冲突
    for (Entry e = tab[i];
         e != null;
         e = tab[i = nextIndex(i, len)]) {
        ThreadLocal<?> k = e.get();

        if (k == key) {
            // 找到key，则赋值
            e.value = value;
            return;
        }
		// key == null，但是存在值（因为此处的e != null），说明之前的ThreadLocal对象已经被回收了
        if (k == null) {
            // 用新元素替换旧元素
            replaceStaleEntry(key, value, i);
            return;
        }
    }

    tab[i] = new Entry(key, value);
    int sz = ++size;
    // 清理key== null的Entry，如果不需要清理，请求元素数量大于阀值，则rehash(清理或者扩容)
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
```
<a name="1ypMs"></a>
## get()
```java
public T get() {
    // 获取当前线程
    Thread t = Thread.currentThread();
    // 获取当前线程的成员变量 thread.threadLocals
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        // 从当前线程的ThreadLocalMap获取Entry
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            //获取目标值
            T result = (T)e.value;
            return result;
        }
    }
    // ThreadLocalMap为空或者Entry为空，初始化变量
    return setInitialValue();
}
```
可以看出ThreadLocal中的实现是比较简单的，首先获取当前线程的成员变量threadLocals，然后从threadLocals中获取entry，如果为空，调用setInitialValue初始化变量<br />主要实现方法在map.getEntry(this)中，接下来我们来看一下ThreadLocalMap的getEntry方法
<a name="bjGkN"></a>
### 
<a name="7rPz0"></a>
### ThreadLocalMap#getEntry(ThreadLocal<?> key)
```java
private Entry getEntry(ThreadLocal<?> key) {
    // 获取散列值
    int i = key.threadLocalHashCode & (table.length - 1);
    Entry e = table[i];
    if (e != null && e.get() == key)
        return e;
    else
        return getEntryAfterMiss(key, i, e);
}

// 遍历Entry数组，找到key为this的value，顺便清理key为null的值，并重新散列数组
private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
    Entry[] tab = table;
    int len = tab.length;

    while (e != null) {
        ThreadLocal<?> k = e.get();
        if (k == key)
            return e;
        if (k == null)
            expungeStaleEntry(i);
        else
            i = nextIndex(i, len);
        e = tab[i];
    }
    return null;
}
```
<a name="O8owx"></a>
## remove()
```java
public void remove() {
    ThreadLocalMap m = getMap(Thread.currentThread());
    if (m != null)
        m.remove(this);
}
// ThreadLocalMap
private void remove(ThreadLocal<?> key) {
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);
    for (Entry e = tab[i];
         e != null;
         e = tab[i = nextIndex(i, len)]) {
        if (e.get() == key) {
            // entry.key = null
            e.clear();
            // 	清理key为null的entry，并重新散列数组
            expungeStaleEntry(i);
            return;
        }
    }
}
```
<a name="9XnGR"></a>
# 
<a name="1Ptkc"></a>
# 内存泄漏分析
执行如下代码
```java
public class ThreadLocalTest {

    private static final int THREAD_LOOP_SIZE = 500;
    private static final int MOCK_DIB_DATA_LOOP_SIZE = 10000;

    private static ThreadLocal<List<User>> threadLocal = new ThreadLocal<>();


    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_LOOP_SIZE);

        for (int i = 0; i < THREAD_LOOP_SIZE; i++) {
            executorService.execute(() -> {
                threadLocal.set(new ThreadLocalTest().addBigList());
                Thread t = Thread.currentThread();
                System.out.println(Thread.currentThread().getName());
                //threadLocal.remove(); //不取消注释的话就可能出现OOM
            });
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //executorService.shutdown();
    }

    private List<User> addBigList() {
        List<User> params = new ArrayList<>(MOCK_DIB_DATA_LOOP_SIZE);
        for (int i = 0; i < MOCK_DIB_DATA_LOOP_SIZE; i++) {
            params.add(new User("xuliugen", "password" + i, "男", i));
        }
        return params;
    }
    class User {
        private String userName;
        private String password;
        private String sex;
        private int age;

        public User(String userName, String password, String sex, int age) {
            this.userName = userName;
            this.password = password;
            this.sex = sex;
            this.age = age;
        }
    }
}
```

- 第一次执行把17行注释，每次线程执行完后不会移除ThreadLocalMap中的Entry，就导致线程任务执行完成之后，还在持有ThreadLocalMap的引用，ThreadLocalMap里的Entry数组中的value一直强引用着list对象，导致线程数量执行较多的时候，堆内存就会急剧增加，导致OOM

![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590675160190-e2d00d02-b377-49cb-8fc0-14571bccbef2.png#align=left&display=inline&height=212&margin=%5Bobject%20Object%5D&name=image.png&originHeight=276&originWidth=962&size=431032&status=done&style=none&width=744)<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590675137430-7b62ced5-0dc9-438f-8458-9f76ff57ba48.png#align=left&display=inline&height=750&margin=%5Bobject%20Object%5D&name=image.png&originHeight=750&originWidth=900&size=232368&status=done&style=none&width=900)<br />

- 第二次执行是把17行注释去掉，每次线程任务执行完成后，都会调用ThreadLocal的remove方法移除Thread中的ThreadLOcalMap对ThreadLocal的引用，并清理value值，因为此时value的可以为null，在remove方法内会调用expungeStaleEntry去掉对value的引用，堆内存就不会一直增加，就不会导致OOM


<br />
<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590675305341-709e0b42-ac01-43cb-97a6-7923697ff8ea.png#align=left&display=inline&height=273&margin=%5Bobject%20Object%5D&name=image.png&originHeight=273&originWidth=625&size=216148&status=done&style=none&width=625)<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590675328866-63339ca3-c505-4d14-9ac2-74b209b5b3ce.png#align=left&display=inline&height=750&margin=%5Bobject%20Object%5D&name=image.png&originHeight=750&originWidth=900&size=271657&status=done&style=none&width=900)<br />
<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1590718471742-39a44c31-ab1f-482f-b38b-384027f0f0bd.png#align=left&display=inline&height=353&margin=%5Bobject%20Object%5D&name=image.png&originHeight=353&originWidth=780&size=64391&status=done&style=none&width=780)<br />

> [https://www.jianshu.com/p/ee8c9dccc953](https://www.jianshu.com/p/ee8c9dccc953)
> [https://juejin.im/post/5d9d74fa6fb9a04e320a56db](https://juejin.im/post/5d9d74fa6fb9a04e320a56db)
> [http://cmsblogs.com/?p=2442](http://cmsblogs.com/?p=2442)
> [https://blog.csdn.net/xlgen157387/article/details/78298840](https://blog.csdn.net/xlgen157387/article/details/78298840)


[下一篇：AQS](06-J.U.C/01-AQS/00-AQS.md)