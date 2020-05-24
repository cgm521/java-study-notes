# ReentrantLock源码分析

<a name="pW8sr"></a>
# 1.概述
ReentranLock是一种可重入的互斥锁，也称为**独占锁**，ReentrantLock可被同一个线程多次获取。ReentranLock又分为公平锁“FairSync”和非公平锁“NonfairSync”，他们区别于获取锁的机制上。在公平锁中，是按照同步队列中的排列顺序依次获取锁；而非公平锁中，只要锁是可获取状态的，不管自己是不是在队列的head位置，都会尝试获取锁。<br />

<a name="kI6Qd"></a>
# 2.数据结构与实现
ReentranLock实现Lock接口，ReentranLock中有内部抽象类Sync继承AbstractQueuedSynchronizer。ReentranLock通过实现Lock接口的方法提供给外部操作锁，又通过继承AbstractQueuedSynchronizer来实现对锁的获取和释放。
```java
public interface Lock {
    //获取锁，如果锁不可用则线程一直等待
    void lock();
    //获取锁，响应中断，如果锁不可用则线程一直等待
    void lockInterruptibly() throws InterruptedException;
    //获取锁，获取失败直接返回
    boolean tryLock();
    //获取锁，等待给定时间后如果获取失败直接返回
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
    //释放锁
    void unlock();
    //创建一个新的等待条件
    Condition newCondition();
}
```
ReentranLock中静态内部类FairSync实现Sync，实现lock方法，实现公平锁的获取锁方法；静态内部类NonfairSync实现Sync，实现lock方法，实现非公平锁的获取锁方法。

```java
static final class NonfairSync extends Sync {
        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * Sync object for fair locks
     */
    static final class FairSync extends Sync {
        final void lock() {
            acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
```
公平锁与非公平锁的不同只有在获取锁的时候，**lock()和tryAcquire()**两个方法
<a name="V0i6P"></a>
##
**ReentrantLock有两个构造函数**
```java
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
```
默认使用来非公平锁，另一个是通过参数fair来决定是使用公平锁还是非公平锁
> 为什么默认是非公平锁？

- 这是因为非公平锁效率高，因为在公平锁模式下，如果A请求获取锁，即使现在锁是可获取的，A也需要排在同步队列队尾；需要先唤醒头节点后的节点H，把H节点从挂起到运行状态，然后才能去获取锁，而H节点从挂起到挂起状态需要较长的时间，这段空闲时间就会白白浪费，而非公平锁可用在这段空闲时间获取锁，完成自己的任务，释放锁后H再去获取锁，而且H获取锁也没有推迟，大大的提高来吞吐量


<br />

<a name="PWhb2"></a>
# 3.源码解析
<a name="51PB2"></a>
## 1.lock()
**lock用于获取锁，如果获取失败，则阻塞，直到获取成功，不响应中断**
```java
//ReentrantLock 实现Lock
public void lock() {
    sync.lock();
}

// FairSync 实现Sync
final void lock() {
    acquire(1);
}

// NonfairSync 实现Sync
final void lock() {
    if (compareAndSetState(0, 1))
        setExclusiveOwnerThread(Thread.currentThread());
    else
        acquire(1);
}
```
ReentrantLock的lock方法是实现Lock，会调用内部类Sync的lock方法，Sync的lock是个抽象方法，实现类FairSync与NonfairSync有格子不同的实现，

- FairSync中直接调用AQS中的acquire方法获取锁
- NonfairSync中会先尝试CAS直接获取锁，获取失败再通过调用AQS中的acquire方法获取锁



公平锁和非公平锁都会调用AQS的acquire方法获取锁，之前分析AQS中acquire方法调用的tryAcquire方法需要子类实现，在公平锁和非公平锁有不同的实现。
<a name="XyrPa"></a>
### 公平锁的tryAcquire
```java
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    // 获取锁状态
    int c = getState();
    if (c == 0) {
        // 锁没有被占用
        // 判断当前节点是否是头节点或头节点的后继节点(即将获取锁)
        if (!hasQueuedPredecessors() &&
            // CAS获取锁
            compareAndSetState(0, acquires)) {
            // 设置当前线程为持有锁的线程
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        // 锁已被占用，判断是否是当前线程持有
        int nextc = c + acquires;
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        // 重入，已获取锁，不存在竞争
        setState(nextc);
        return true;
    }
    return false;
}
```
说明

- 如果当前锁状态为**0**，表示锁没有被占用。然后调用hasQueuedPredecessors判断是否还有前节点在等待获取锁，如果有，返回false
  - 如果没有，则调用compareAndSetState修改state值，标记自己获取到锁，然后调用setExclusiveOwnerThread设置当前线程为持有锁的线程，获取锁成功，返回true
- 如果当前锁状态不为0，且持有锁的为当前线程(current == getExclusiveOwnerThread())，由于锁是可重入的，直接更新state，返回rtue
<a name="qpjED"></a>
### 非公平锁的tryAcquire
直接调用Sync的nonfairTryAcquire方法
```java
protected final boolean tryAcquire(int acquires) {
    return nonfairTryAcquire(acquires);
}

// Sync
final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (compareAndSetState(0, acquires)) {// 与公平锁不同点
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```
非公平锁点tryAcquire实现与公平锁中的实现唯一的不同是在**compareAndSetState之前没有调用hasQueuedPredecessors方法判断是否有前节点在等待获取锁**，而是**直接获取锁**<br />

<a name="SSbWO"></a>
## 2.lockInturruptibly()
响应中断式的获取锁，公平锁与非公平锁实现一样
```java
public void lockInterruptibly() throws InterruptedException {
    sync.acquireInterruptibly(1);
}

// AQS
public final void acquireInterruptibly(int arg)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    if (!tryAcquire(arg))
        doAcquireInterruptibly(arg);
}
```

<br />lockInterruptibly会直接调用AQS的acquireInterruptibly方法，之前分析过，该方法会响应中断，调用的tryAcquire方法，上面分析过了<br />

<a name="DEpOr"></a>
## 3.tryLock()
尝试获取锁，获取失败就返回false<br />**即使是公平锁，也会尝试获取锁，不会把线程添加到同步队列排队**
```java
public boolean tryLock() {
    return sync.nonfairTryAcquire(1);
}
// Sync
final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```
说明

- tryLock直接调用Sync中的nonfairTryAcquire方法获取锁，成功则返回true，失败则返回false
- 不涉及到队列操作
<a name="ZmRuP"></a>
## 4.tryLock(long timeout, TimeUnit unit)
带超时的获取锁，响应中断<br />

```java
public boolean tryLock(long timeout, TimeUnit unit)
        throws InterruptedException {
    return sync.tryAcquireNanos(1, unit.toNanos(timeout));
}
public final boolean tryAcquireNanos(int arg, long nanosTimeout)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    return tryAcquire(arg) ||
        doAcquireNanos(arg, nanosTimeout);
}
```
说明：

- 直接调用AQS中的tryAcquireNanos获取锁，会响应中断，带有超时检查
- 区别是tryAcquire方法在FairSync和NonfairSync中的实现不同
<a name="pyI7Z"></a>
## 5.unLock
对于释放锁来说公平锁与非公平锁是一样的，所以定义在Sync中<br />

```java
// 实现Lock
public void unlock() {
    sync.release(1);
}

// AQS
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}

// Sync
protected final boolean tryRelease(int releases) {
    // 还剩下state值
    int c = getState() - releases;
    //判断当前线程是否是锁持有者
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        // state为0，代表全部释放，设置锁持有者为 null
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```
说明：

- 释放锁，直接调用AQS中release方法，调用tryRelease方法释放锁，
- tryRelease由AQS的继承类Sync实现，



<a name="muvVf"></a>
## 6.newCondition


```java
// 实现Lock
public Condition newCondition() {
    return sync.newCondition();
}

// Sync
final ConditionObject newCondition() {
    return new ConditionObject();
}
```
说明：

- ReentrantLock本身并没有实现Condition，是使用AQS中的ConditionObject类



<a name="04YIx"></a>
# 总结
ReentrantLock中大多数方法都是调用AQS中的方法，AQS中已经完成了绝大多数逻辑的实现，子类只需要继承去使用就可以<br />
<br />
<br />

> [https://www.jianshu.com/p/38fe92bcca7e](https://www.jianshu.com/p/38fe92bcca7e)
> [https://segmentfault.com/a/1190000016503518#item-2](https://segmentfault.com/a/1190000016503518#item-2)
> [http://cmsblogs.com/?p=2210](http://cmsblogs.com/?p=2210)


<br />

