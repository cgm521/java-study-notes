# 概述
CyclicBarrier是一个同步辅助器，他允许一组线程互相等待，直到到达某个屏障点。如果一个程序中有固定的线程，并且需要相互等待，这时候适合使用CyclicBarrier。CyclicBarrier是可以重复使用的，就是说一组线程到达屏障点后，线程释放，CyclicBarrier就会重置，下一批线程来到还可以继续使用CyclicBarrier进行阻塞
比如去游乐园玩过山车，要10个人(固定的线程)才能开一次，那么来到的人就需要在栅栏门口(屏障点)等着，等到10个人了，才会开，然后会栅栏落下，继续等待下一批。


# 数据结构与方法
### 核心属性
```java
private static class Generation {
    boolean broken = false;
}

/** The lock for guarding barrier entry */
private final ReentrantLock lock = new ReentrantLock();
/** Condition to wait on until tripped */
private final Condition trip = lock.newCondition();
/** The number of parties */
private final int parties;
/* The command to run when tripped */
private final Runnable barrierCommand;
/** The current generation */
private Generation generation = new Generation();

/**
 * Number of parties still waiting. Counts down from parties to 0
 * on each generation.  It is reset to parties on each new
 * generation or when broken.
 */
private int count;
```
CyclicBrrier核心属性有6个
**线程个数**

- parties：代表了线程总数，即**需要一同通过CyclicBrrier的线程数，它是final类型，由构造函数初始化，在类创建后就不会变了**
- count：和CountDownLatch中count一样，**代表着还需等待多少线程**，每个线程来到会减一，如果该值为0，则代表所有线程到齐了，可以一起通过屏障点了

**基础实现**
```java
private final ReentrantLock lock = new ReentrantLock();
private final Condition trip = lock.newCondition();
private Generation generation = new Generation();
```
CyclicBrrier是基于独占锁ReentrantLock和条件队列实现的，所有互相等待的线程都会在同一个条件队列trip上挂起，被唤醒后添加到同步队列中去竞争锁lock，获取到锁的线程会继续往下执行。
Generation对象是表示CyclicBrrier的代，同一代的CyclicBrrier的Generation是相同的，其内部只有一个boolean类型属性broken，代表着这一代是否损坏。
private final Runnable barrierCommand;
barrierCommand是一个Runnable对象，代表着一个任务，所有线程都到达屏障点后，需要执行的动作，有点类似于钩子方法。


### 构造函数
```java
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}

public CyclicBarrier(int parties) {
    this(parties, null);
}
```
CyclicBrrier有两个构造函数，一个是不传barrierAction，就是到达屏障点后不需要执行额外操作，
构造函数就是初始化parties、count和barrierAction三个变量


# 源码分析
## 辅助方法
要理解CyclicBarrier，首先我们需要弄明白它的几个辅助方法。


首先需要理解的是“代”（Generation）的概念，由于CyclicBarrier是可重复使用的，我们把每一个新的barrier称为一“代”。这个怎么理解呢，打个比方：**一个过山车有10个座位，景区常常需要等够10个人了，才会去开动过山车。于是我们常常在栏杆（barrier）外面等，等凑够了10个人，工作人员就把栏杆打开，让10个人通过；然后再将栏杆归位，后面新来的人还是要在栏杆外等待。这里，前面已经通过的人就是一“代”，后面再继续等待的一波人就是另外一“代”，栏杆每打开关闭一次，就产生新一的“代”。**
在CyclicBarrier，开启新的一代使用的是nextGeneration方法：
### nextGeneration()
```java
private void nextGeneration() {
    // 唤醒当前这一代的所有在条件队列中等待的线程
    trip.signalAll();
    // 重置count的值
    count = parties;
    // 开启下一代
    generation = new Generation();
}
```
该方法用于开启新的一代，通常是最后一个调用await方法的线程调用。在该方法中主要是唤醒这一代等待中的线程，然后将count的值恢复成parties，以及开启新的一代
### breakBarrier
```java
private void breakBarrier() {
    // 标记broken状态
    generation.broken = true;
    // 恢复count值
    count = parties;
    // 唤醒当前这一代所有等待在条件队列里的线程(此时线程已经被打破)
    trip.signalAll();
}
```
breakBarrier就是打破现有的栅栏，让所有线程都通过。
这个breakBarrier怎么理解呢，继续拿上面过上车的例子打比方，有时候某个时间段，景区的人比较少，等待过山车的人数凑不够10个人，眼看后面迟迟没有人再来，这个时候有的工作人员也会打开栅栏，让正在等待的人进来坐过山车。这里工作人员的行为就是`breakBarrier`，由于并不是在凑够10个人的情况下就开启了栅栏，我们就把这一代的`broken`状态标记为`true`。
### reset
```java
public void reset() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        breakBarrier();   // break the current generation
        nextGeneration(); // start a new generation
    } finally {
        lock.unlock();
    }
}
```

- reset方法就是将barrier恢复成初始状态，内部就是调用了breakBarrier和nextGeneration方法
- 该方法执行前需要获取锁，
- 如果在该方法执行时，有线程正在等待barrier，则会立即抛出BrokenBarrierException异常
## await
```java
public int await() throws InterruptedException, BrokenBarrierException {
    try {
        return dowait(false, 0L);
    } catch (TimeoutException toe) {
        throw new Error(toe); // cannot happen
    }
}

public int await(long timeout, TimeUnit unit)
    throws InterruptedException,
           BrokenBarrierException,
           TimeoutException {
    return dowait(true, unit.toNanos(timeout));
}
```
await有两种形式，一种是阻塞时等待，一种是带超时的等待，都是调用带超时机制的dowait方法实现


### dowait
```java
private int dowait(boolean timed, long nanos)
    throws InterruptedException, BrokenBarrierException,
           TimeoutException {
    final ReentrantLock lock = this.lock;
    // 执行await方法的线程必须要获取到锁
    lock.lock();
    try {
        final Generation g = generation;
        if (g.broken)
            // 调用过breakBarrier方法破坏了代
            throw new BrokenBarrierException();
        if (Thread.interrupted()) {
            // 当前线程被中断。则需要将栅栏打破，然后抛出中断异常
            // 因为所有等待barrier的线程都是相互等待的，如果当前线程被中断，
            // 那么就会可能永远也能凑不够parties个线程到达屏障点了，
            // 所以需要打破栅栏，通知其他线程不需要在等待了
            breakBarrier();
            throw new InterruptedException();
        }
        // 当前线程已经到达屏障点，计数减一
        int index = --count;
        // 当前等待线程为0，表示所有线程都已到达屏障点，可以唤醒所有线程，通过栅栏，重置栅栏了
        if (index == 0) {  // tripped
            // 执行到达屏障点后的额外动作
            boolean ranAction = false;
            try {
                final Runnable command = barrierCommand;
                if (command != null)
                    command.run();
                ranAction = true;
                // 唤醒所有线程，开启下一代
                nextGeneration();
                return 0;
            } finally {
                if (!ranAction)
                    // 额外动作执行失败，打破当前这一代
                    breakBarrier();
            }
        }
        // 如果当前count不为0，就将当前线程挂起，直到所有线程到齐或者等待超时，或者有线程被中断
        for (;;) {
            try {
                if (!timed)
                    trip.await();
                else if (nanos > 0L)
                    nanos = trip.awaitNanos(nanos);
            } catch (InterruptedException ie) {
                // 执行到这里说明当前线程被中断了
                // 如果线程被中断了还属于这一代，
                // 那么需要先打破栅栏，然后抛出中断异常
                if (g == generation && ! g.broken) {
                    breakBarrier();
                    throw ie;
                } else {
                    // 执行到这里说明当前代已经破坏或者已经产生新的一代
                    // 一种情况是新的一代已经产生g!=generation,所以不需要再去打破栅栏，
                    // 只需要自我中断一下就可以
                    // 另一种是g.broken==true，说明中断前栅栏已经被破坏，
                    // 那么也不需要当前线程去破坏了
                    Thread.currentThread().interrupt();
                }
            }
            // 执行到这里说明当前线程被唤醒了

            // 先检测broken状态，能让broken变为true的只有breakBarrier方法，场景有
            // 1.其他执行await的方法的线程在挂起前就被中断了
            // 2.其他执行await的方法的线程在等待中被中断了
            // 3.最后一个到达屏障点的线程执行barrierCommand的时候失败了
            // 4.reset()方法被调用
            if (g.broken)
                throw new BrokenBarrierException();

            // 执行到这里说明说明线程被唤醒，并且新的一代已经产生，一切正常，
            // 返回当前线程执行await时剩余多少线程
            if (g != generation)
                return index;
            // 如果是超时被唤醒，则打破栅栏，抛出超时异常
            if (timed && nanos <= 0L) {
                breakBarrier();
                throw new TimeoutException();
            }
        }
    } finally {
        lock.unlock();
    }
}
```
这个await方法包揽了countDown、阻塞线程、唤醒线程、执行barrierCommand任务、开启新一代，处理中断等诸多任务
值得注意的是，**await方法是有返回值的，代表了线程到达的顺序**，第一个到达的线程的index为`parties - 1`，最后一个到达的线程的index为`0`


# 代码示例
```java

public class CyclicBarrierDemo {
    public static void main(String[] args) throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(5,()-> System.out.println("执行完毕"));
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(new BarrierWork(cyclicBarrier));
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println();
    }
}

class BarrierWork implements Runnable {
    private CyclicBarrier cyclicBarrier;

    public BarrierWork(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "-" + "start await,count:" + cyclicBarrier.getNumberWaiting());
        try {
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "-" + "通过栅栏");
    }
}
```
![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1592840237422-a8c48af5-87c3-4f28-8121-11704c8f2b04.png#height=233&id=n1cZC&margin=%5Bobject%20Object%5D&name=image.png&originHeight=466&originWidth=792&originalType=binary&size=51141&status=done&style=none&width=396)


# 总结

- CyclicBarrier实现了类似于CountDownLatch的功能，它可以让一组线程互相等待，直到所有线程都到齐了才继续往下执行。
- CyclicBarrier是基于独占锁和条件队列实现，而非共享锁
- CyclicBarrier可重复使用，在所有线程到齐后，就会开启新的一代
- CyclicBarrier采用“all-or-none breakage model”，所有相互等待的线程要么一起通过屏障点，要么一个也通过不了；如果有一个线程被中断，失败或超时等待而过早的退出等待，则该barrier会被破坏，所有等待的线程都会抛出异常







> 引用
> [https://segmentfault.com/a/1190000016518256#item-1](https://segmentfault.com/a/1190000016518256#item-1)

