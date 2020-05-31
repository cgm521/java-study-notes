# volatile

<a name="o7lIb"></a>
#  定义
Java 语言提供了一种稍弱的同步机制，即 volatile 变量，用来确保将变量的更新操作通知到其他
线程。volatile 变量具备两种特性，volatile 变量不会被缓存在寄存器或者对其他处理器不可见的
地方，因此在读取 volatile 类型的变量时总会返回最新写入的值
<br />

<a name="FdX0v"></a>
##  变量可见性

- 保证该变量对所有线程都可见，当一个线程修改了该变量，新的值对其他线程可以立即获取到
- 通过LOCK#指令和缓存一致性协议实现
<a name="BDNmj"></a>
### LOCK作用

- 锁总线，其他CPU对内存的读写请求都会阻塞，直到锁释放，由于锁总线开销较大，后期都改为**锁缓存代替锁总线**
- lock后的写操作会回写已修改的数据，且让其他CPU相关缓存失效，从而重新从主内存加载最新的值
- 不是内存屏障却能完成内存屏障的功能，阻止屏障两边的指令重排序

**锁缓存代替锁总线，这种场景下的数据一致性是通过缓存一致性协议来保证的**
<a name="a2QU1"></a>
### 缓存一致性

- 缓存是分段的，一个段对应着一块存储空间，我们称之为缓存行，它是CPU缓存中可分配段最小存储单元。**当CPU看到一条读取内存的指令时，它会把内存地址传递给一级数据缓存，一级数据缓存会检查它是否有这个内存地址对应的缓存段，如果没有就把整个缓存段从内存（或更高一级的缓存）中加载进来**。注意，这里说的是一次加载整个缓存段，这就是上面提过的局部性原理

上面说段锁总线，效率太低，最好段是使用多组缓存，但是他们段的行为看起来就像一组缓存，是一致的。缓存一致性协议就是为了做到这一点，**这类协议就是保证在多组缓存中的内容一致。**<br />缓存一致性协议有很多种，日常使用最多的是属于**嗅探**协议：<br />

- **所有内存的传输是发生在一条共享的总线上的，而所有的处理器都能看到这条总线；**
- **缓存本身是独立的，内存是共享的，所有的内存访问都要经过仲裁（同一个指令周期，只有一个CPU可以访问内存）**
- **CPU缓存不仅是在做内存存储是与总线打交道，而是在不停的嗅探总线上发生的数据交换，跟踪其他缓存在做什么，**
- **所以当一个缓存代表处理器去读内存的时候，其他处理器都会得到通知，他们以此来使自己的缓存保持同步。**
- **当有一个处理器写入内存，其他处理器就会马上知道这块内存在他们的缓存段中失效**

MESI协议是当前最主流的缓存一致性协议，每个缓存行有四个状态，用2bit表示，分别是：<br />


| M(Modifile) | 这行数据有效，数据被修改了，和内存的数据不一致，数据只存在于本Cache中 |
| --- | --- |
| E (Exclusive) | 这行数据有效，数据和内存一样，数据只存在本Cache中 |
| S (Shared) | 这行数据有效，数据和内存一样，数据存在与多个Cache中 |
| I (Invaid) | 这行数据无效 |


<br />这里的I、S、M分别是失效/未载入、干净、脏数据，E是代表着独占式访问，这个状态解决了‘在我们开始修改某块内存之前，需要通知其他处理器’这个问题。只有当缓存行的状态是M或E时在能写入，也就是说只有这两种状态，处理器是独占式缓存的，当处理器想写某个缓存段时，需要给总线发送‘我要独占权’的请求，这回通知其他处理器把拥有同一缓存段的拷贝失效掉。只有在获取独占权后，处理器才开始修改数据，并且处理器此时知道，这个缓存行是只有一份拷贝，就是在自己的缓存里，所以不会有冲突。<br />如果有其他处理器想要读取这个缓存行（独占的处理器会立刻知道，因为一直在嗅探总线），独占或已修改的缓存行，必须要回到共享状态，如果是已修改的缓存行，还需要把内容回写到内存中。<br />

<a name="uvy8l"></a>
## 由LOCK#看volatile的读写
那么当两条线程T-A与T-B同时操作volatile变量i时，T-A写了变量i：

- T-A向总线发出LOCK#指令
- 发出的LOCK#指令锁缓存行，同时让T-B中缓存中的缓存行内容失效
- T-A向主内存回写最新的i

T-B读变量i：<br />- T-B发现对应的地址的缓存行被锁，等待锁释放，缓存一致性协议会保证他读取到最新的变量值
<a name="9i4PD"></a>
## 有序性
<a name="cHYsA"></a>
###  happens-before规则
**happens-before**规则中有一条关于volatile变量规则：**对一个volatile变量的写，happens-before于任意后续对这个变量的读**<br />

```java
//假设线程A执行writer方法，线程B执行reader方法
class VolatileExample {
    int a = 0;
    volatile boolean flag = false;

    public void writer() {
        a = 1;              // 1 线程A修改共享变量
        flag = true;        // 2 线程A写volatile变量
    }

    public void reader() {
        if (flag) {         // 3 线程B读同一个volatile变量
        int i = a;          // 4 线程B读共享变量
        ……
        }
    }
}
```


- 根据**happens-before**规则，会建立3类happens-before关系
  - 根据**程序次序**规则：1 happens-before 2 且 3 happens-before 4
  - 根据**volatile**规则：2 happens-before 3
  - 根据**传递性**规则：1 happens-before 4
- 因为以上规则，当线程A修改volatile变量a后，线程B会立刻感知



<a name="o7W5q"></a>
### 禁止指令重排序

- 为了性能优化，JMM会在不改变正确语义的前提下，允许编译器和处理器对指令序列进行重排序。JMM提供了内存屏障阻止这种重排序
- java编译器会在生成指令系列时适当的插入内存屏障来禁止指定类型的处理器重排序
- JMM采用来最保守对策略
  - 在每个volatile写操作的前面插入一个StoreStore屏障，禁止上面的普通写与volatile写重排序
  - 在每个volatile写操作的后面插入一个StoreLoad屏障，禁止下面的volatile读/写与volatile写重排序
  - 在每个volatile读操作的后面插入一个LoadLoad屏障，禁止下面的普通读与volatile读重排序
  - 在每个volatile读操作的后面插入一个LoadStore屏障，禁止下面读普通写与volatile读重排序

![image.png](https://cdn.nlark.com/yuque/0/2019/png/261655/1576653581528-83988d97-5d50-45b9-8f72-62b41773b01b.png#align=left&display=inline&height=461&name=image.png&originHeight=461&originWidth=747&size=80913&status=done&style=none&width=747)![image.png](https://cdn.nlark.com/yuque/0/2019/png/261655/1576653590957-9c5a34e8-b061-4341-a064-4d442b30ad67.png#align=left&display=inline&height=459&name=image.png&originHeight=459&originWidth=801&size=82658&status=done&style=none&width=801)
<a name="bVJVZ"></a>
## 使用场景

- 对变量的写操作不依赖于当前值（i++），或单纯的变量赋值（flag=true）
- 该变量没有包含在具有其他变量的不变式中，不同volatile不能相互依赖。只有状态真正独立于程序内其他内容时，才能使用volatile


<br />
<br />**


[下一篇：05-ThreadLocal.md](05-ThreadLocal.md)