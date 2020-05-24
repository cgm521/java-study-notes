# AQS-独占式锁获取&amp;释放

<a name="g1vko"></a>
# acquire -- 获取同步状态-不响应中断
![untitled.jpg](https://cdn.nlark.com/yuque/0/2019/jpeg/261655/1569245265433-79edbb17-68b2-4d47-b3bd-6b0a47ff241a.jpeg#align=left&display=inline&height=638&margin=%5Bobject%20Object%5D&name=untitled.jpg&originHeight=850&originWidth=624&size=71008&status=done&style=none&width=468)<br />acquire(int arg)方法为AQS提供的模版方法，该方法为独占式获取同步状态，但是对中断不响应<br />**其他线程尝试通过interrupt中断当前线程，当前线程不会立刻中断，也不会抛异常，只是记录一个中断标志，外部可通过isInterrupted方法，获取这个中断标志，然后做些什么**
```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```
该方法涉及到四个方法

- **tryAcquire**：尝试获取同步状态，获取成功返回true，否则返回false。**该方法由自定义同步组件自己实现**，该方法必须保证可以线程安全的获取同步状态
- **addWaiter**：如果tryAcquire返回false，则将当前线程包装成Node，放到同步队列队尾，并返回该node
- **acquireQueued**：当前线程根据公平性原则进行阻塞等待(自旋，主要做两件事情)，并返回是否中断过
  - 如果当前节点的前驱节点是head节点，则尝试获取同步状态
  - 将当前线程挂起，使CPU不在调度它
- **selfInterrupt**：产生一个中断，用于中断当前线程
  - 由于acquire方法不响应中断，如果在抢锁的过程中发生了中断，AQS会记录是否发生过中断，如果发生过中断，则在退出acquire之前，调用selfInterrupt方法自我中断一下



<a name="Q7lmE"></a>
## addWaiter
> - 把当前Thread包装成Node节点并插入同步队列队尾，并返回

```java
// addWaiter(Node.EXCLUSIVE)  Node.EXCLUSIVE = null;

private Node addWaiter(Node mode) {
    // 把当前线程包装成Node节点
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    // 快速尝试添加尾节点
    if (pred != null) {
        //  0.设置当前节点的前驱节点为尾节点
        node.prev = pred;
        /** 1.CAS设置当前节点为尾节点，2.设置前驱节点的后继节点为当前节点，
         * 注意这两个操作不是原子操作，会存在设置完队尾后，还没有把前驱节点的后继节点设为当前节点
         * 就有另一个线程遍历同步队列，如果从头到尾遍历，会找不到队尾，所以AQS中为从尾到头遍历队列
         */
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    /* 代码执行到这里有两种情况
     *	1.队列为空 2.其他线程改变tail值，导致CAS失败
     */
    enq(node);// 将节点插入队列
    return node;
}
```

<br />addWaiter方法首先尝试直接把节点插入到同步队列队尾，但是存在并发情况，如果其他线程已经把tail修改，此时CAS会失败，则会调用enq，采用CAS+自旋的方式把节点插入队列
<a name="1AXxp"></a>
### enq
> - 自旋的方式把Node节点插入同步队列队尾，并返回

```java
private Node enq(final Node node) {
    // 自旋
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
            // 如果队列是空的，先初始化-- new一个空节点，设置为头节点
            // 可以看出同步队列是采用懒加载的方式，以提高性能
            if (compareAndSetHead(new Node()))
                tail = head;// 将尾节点指向头节点，不返回
        } else {
            // 队列不是空的，尝试把节点加入队尾
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```


> **注意！知识点：把节点加入队尾操作不是一个原子操作，它包括三步，**
> 1. **把当前节点的前驱节点设为老的尾节点，**
> 1. **CAS设置尾节点为当前节点，**
> 1. **把老的尾节点的后继节点设为当前节点。**
>
**会存在以下两种情况破坏原子操作**
> 1. **T1执行完第一步后，T2线程把tail改成另一个节点，导致CAS失败，则重新循环**
> 1. **T1执行完第一第二步，T2线程抢占时间片，执行入队操作，不影响线程安全，最终一致性**
>
**第二种破坏原子操作虽然不会影响线程安全，但是如果T1执行完1，2后，T3过来遍历同步队列，如果是从头到尾遍历，会找不到新加入的尾节点，so，AQS中遍历同步队列都是从尾到头遍历**



<a name="96D9F"></a>
## acquireQueued
> - 自旋方式获取锁

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        // 中断标志
        boolean interrupted = false;
        // 自旋
        for (;;) {
            // 获取当前节点的前驱节点
            final Node p = node.predecessor();
            // 当前节点的前驱节点是头节点，则再次尝试获取同步状态
            if (p == head && tryAcquire(arg)) {
                // 获取成功，把当前节点设为头节点
                // 相当于是把当前节点出队
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
           }
            // 失败后，判断是否需要挂起
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        // acquire不响应中断，执行结束的前提条件就是获取到同步状态，failed一定是false，
        // 所以这个肯定不会执行，这个方法是为响应中断式的抢锁准备的，后面再说
        if (failed)
            cancelAcquire(node);
    }
}
// setHead
private void setHead(Node node) {
    head = node;
    node.thread = null;
    node.prev = null;
}
```

<br />首先是获取前驱节点，如果前驱节点是head，说明当前节点已经排到了最前面，那么尝试再次获取锁，成功则把当前节点设为头节点，返回中断状态；否则调用判断是否需要挂起当前线程
> **注意！知识点：****setHead方法为普通方法，没有采用cas，难道不怕线程不安全？**
> **因为这里不会存在竞争！！！**
> **能走到这一步，说明已经获取到同步状态，不会有并发情况**

_`setHead方法把节点线程设为null，head为已经获取到同步状态的节点，就不需要在维护这个线程，因此就head节点的thread总是null`_
<a name="fVaHx"></a>
### shouldParkAfterFailedAcquire
> - 该方法的最终目的是为当前节点找到一个未被取消的节点，挂在它后面，并把前驱节点状态设为SIGNAL



```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        /*
         * 如果前驱节点的状态为SIGNAL，返回true，线程应该挂起
         */
            return true;
        if (ws > 0) {
            /*
             * 前驱节点被取消，往前遍历，直到找到未取消状态的节点
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            // 当前节点放到该节点后面
            pred.next = node;
            // node前面的取消的节点完成出队
        } else {
            /*
             * waitStatus 是 0 or PROPAGATE.  把前驱节点的状态改为SIGNAL，返回false
             * acquireQueued方法中再次自旋，进入该方法时，前驱节点就是SIGNAL，返回true
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```


> **注意！知识点：为什么要设置前驱节点的状态为SIGANL呢？**
> **节点的SIGNAL状态都是后继节点为当前节点设置的，后继节点把当前节点的状态设为SIGNAL后，就挂起了(或者准备挂起)，然后当当前节点释放同步状态或取消时，会唤醒后继节点，使后继节点重新进入自旋，尝试获取锁**

<a name="uyeDd"></a>
### parkAndCheckInterrupt
> - 中断当前线程



```java
// shouldParkAfterFailedAcquire返回true，线程需要挂起，会执行到这个方法
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);// <-- 线程被挂起，不再往下执行！！！！

    // 从这里被唤醒，有两种方法，1：前驱节点唤醒，
    //						2:其他线程调用当前线程的interrupt方法，中断当前线程
    // 注意！！！！当前驱节点唤醒当前节点，才会往下执行，走到这一步！！！
    // 这个函数将返回当前正在执行的线程的中断状态，并清除它
    // 第一种方法唤醒，返回false；第二种方法唤醒，返回true，并清除中断状态
    // selfInterrupt()会重新设置中断标志
    return Thread.interrupted();
}
```


<a name="uXNxv"></a>
### acquireQueued总结

- 自旋过程中先判断当前节点的前驱节点是否是Head节点，如果是，则尝试再次获取同步状态，
- 失败后，则调用shouldParkAfterFailedAcquire判断当前节点是否需要挂起，如果前驱节点当状态为SIGANAL，返回true，否则，找到未取消状态的节点，CAS设置为SIGNAL，返回false
- 需要挂起，则调用parkAndCheckInterrupt挂起当前线程，等待被唤醒
- 返回false，继续自旋



<a name="fle4Q"></a>
## selfInterrupt
> - 为当前线程设置一个中断标志



```java
static void selfInterrupt() {
    // 中断当前线程
    // parkAndCheckInterrupt返回true，代表有其他线程要中断当前线程，这里设置一下中断状态，
    // 外部可以通过Thread.currentThread().isInterrupted()获取中断状态，去优雅的中断线程
    Thread.currentThread().interrupt();
}
```
如果前面环节的parkAndCheckInterrupt的**中断状态是true，代表有其他线程要中断当前线程**，则会执行当前方法，使线程中断状态设为true<br />因为acquire方法是不响应中断的，所以，执行中只会记录一下中断标志，退出方法之前会补上这个中断<br />`interrupt() 实际上只是给线程设置一个中断标志，线程仍会继续运行。`
<a name="F9jBk"></a>
## acquire总结

- 当前线程首先尝试获取同步状态，成功则获取成功
- 失败则把当前线程包装成一个Node节点，放到同步队列队尾
- 进入自旋环节，
  - 首先判断当前节点的前驱节点是否是Head，如果是则尝试再次获取同步状态，成功则把当前节点设置为头节点，退出自旋
  - 失败则判断当前节点是否需要挂起，需要则中断当前线程，等待前驱节点唤醒
  - 不需要则继续自旋
- 退出自旋说明已经获取到同步状态，根据返回状态判断是否需要自我中断

---

<a name="N7Kd6"></a>
# release -- 释放同步状态


```java
public final boolean release(int arg) {
    // 释放同步状态
    if (tryRelease(arg)) {
        Node h = head;
        // 头节点不为null && 头节点的等待状态不为0
        // 头节点的状态为0，则代表后继节点没有需要唤醒的
        if (h != null && h.waitStatus != 0)
            // 唤醒后继节点
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```
<a name="2OvPK"></a>
## unparkSuccessor
> - **_唤醒距head最近的未被取消状态的节点_**

```java
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        // 如果头节点的状态<0，则直接设置为0，节点可以重新竞争资源
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next;
    // 通常情况下，要唤醒的为后继节点
    // 后继节点被取消，则从队尾往前遍历
    // 找到距head节点最近的一个未被取消状态的节点
    if (s == null || s.waitStatus > 0) {
        s = null;
        // 从后往前遍历
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;// 找到后不会return，继续往前遍历
    }
    // 找到了在等待的节点，唤醒它
    if (s != null)
        LockSupport.unpark(s.thread);
}
```


> **我们并不知道线程被唤醒的原因。**
> **具体来说，当我们从`LockSupport.park(this)`处被唤醒，我们并不知道是因为什么原因被唤醒，可能是因为别的线程释放了锁，调用了` LockSupport.unpark(s.thread)`，也有可能是因为当前线程在等待中被中断了，因此我们通过`Thread.interrupted()`方法检查了当前线程的中断标志，并将它记录下来，在我们最后返回`acquire`方法后，如果发现当前线程曾经被中断过，那我们就把当前线程再中断一次（****`实际上只是给线程设置一个中断标志，线程仍会继续运行`****）。**




---



<a name="xigiX"></a>
# acquireInterruptibly -- 响应中断式获取同步状态
**其他线程尝试通过interrupt中断当前线程，当前线程会抛出异常，线程直接中断执行**<br />**
```java
public final void acquireInterruptibly(int arg)
        throws InterruptedException {
    // 先判断是否是中断状态，是则直接抛出异常
    if (Thread.interrupted())
        throw new InterruptedException();
    // 获取同步状态
    if (!tryAcquire(arg))
        // 获取同步状态失败，进入自旋，
        doAcquireInterruptibly(arg);
}
```
<a name="uaFYf"></a>
## doAcquireInterruptibly


```java
private void doAcquireInterruptibly(int arg)
    throws InterruptedException {
    // 当前节点入队
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
        for (;;) {
            final Node p = node.predecessor();
            // 判断当前节点的前驱节点是否是head节点，是则再次尝试获取同步状态
            if (p == head && tryAcquire(arg)) {
                // 获取到同步状态，把当前节点设置为head节点，并把节点的thread设为null，出队
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return;
            }
            // 判断是否需要挂起
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())// 挂起线程
                throw new InterruptedException();//如果线程中断过，唤醒后，直接抛出异常
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

<br />doAcquireInterruptibly方法与acquireQueued方法基本一致，主要的区别是**doAcquireInterruptibly方法响应中断，如果节点线程中断过，唤醒后直接抛出异常，方法抛出异常而退出当前方法，则failed=true，so，cancelAcquire(node)方法会执行**
<a name="En5to"></a>
## cancelAcquire -取消等待
> - 清理被取消状态的节点，使其不再关联Thread，等待状态标为CANCELLED，并出队


<br />**该方法的调用者有**<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/261655/1569498227582-5b8afb78-e794-4080-a3ff-5dec328deb69.png#align=left&display=inline&height=135&margin=%5Bobject%20Object%5D&name=image.png&originHeight=180&originWidth=697&size=233704&status=done&style=none&width=523)<br />**这些方法代码结果都很类似，for(;;)中执行，在finally中判断是否执行cancelAcquire**<br />**
> **cancelAcquire方法的主要作用是清除状态**
> - node不再关联任何Thread
> - node的等待状态设为CANCELLED
> - node出队



```java
private void cancelAcquire(Node node) {
    if (node == null)
        return;
    // 当前节点不在关联任何线程
    node.thread = null;

    // 跳过被取消的节点，找到一个未被取消的节点设为前驱节点
    Node pred = node.prev;
    while (pred.waitStatus > 0)
        node.prev = pred = pred.prev;

    Node predNext = pred.next;

    // 当前节点的等待状态设为取消
    node.waitStatus = Node.CANCELLED;

    // 如果当前节点是尾节点，CAS更新tail为前驱节点，并把前驱节点的后继节点设为null
    // （使当前节点出队）
    if (node == tail && compareAndSetTail(node, pred)) {
        compareAndSetNext(pred, predNext, null);
    } else {
    // If successor needs signal, try to set pred's next-link
    // so it will get one. Otherwise wake it up to propagate.
       int ws;
        // 如果当前线程不是头节点的后继节点，也不是尾节点
        // 把当前节点的前驱节点状态设为SIGNAL，
        // 并使当前节点前驱节点的后继节点设为当前节点当后继节点
        // （使当前节点出队）
        if (pred != head &&
            ((ws = pred.waitStatus) == Node.SIGNAL ||
             (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
            pred.thread != null) {
            Node next = node.next;
            if (next != null && next.waitStatus <= 0)
                compareAndSetNext(pred, predNext, next);
        } else {
            // 当前节点为head当后继节点，直接唤醒当前节点的后继节点
            unparkSuccessor(node);
        }
        node.next = node; // help GC
    }
}
```

<br />分别有三种方式出队，1.node为tail节点 2.node不是tail节点，也不是head的后继节点 3.node为head的后继节点
> **`白色节点代表指向线程为null的节点`**

<a name="1F1gK"></a>
### 场景一、node为tail
![untitled.jpg](https://cdn.nlark.com/yuque/0/2019/jpeg/261655/1569553163137-df172332-3b15-4cec-b41a-7d711cbc5281.jpeg#align=left&display=inline&height=474&margin=%5Bobject%20Object%5D&name=untitled.jpg&originHeight=474&originWidth=510&size=71008&status=done&style=none&width=510)<br />结合代码：

- compareAndSetTail()方法将tail指向pred
- compareAndSetNext()方法将pred的next指向null
> **注意，cancelAcquire()方法只是把pred的next不指向node，但是node的prev还指向pred，根据JVM垃圾回收机制，node节点没有任何地方引用，是不可达状态，因此会被回收**

<a name="suXLr"></a>
### 场景二、node不是tail，也不是head后继节点
![untitled.jpg](https://cdn.nlark.com/yuque/0/2019/jpeg/261655/1569578708327-2af5b3e9-d67b-438a-8264-61096ec55447.jpeg#align=left&display=inline&height=410&margin=%5Bobject%20Object%5D&name=untitled.jpg&originHeight=410&originWidth=890&size=71008&status=done&style=none&width=890)<br />结合代码

- 1->2 compareAndSetNext()方法将pred的next指向successor
> **注意：****cancelAcquire()方法只是操作了next指向，但是prev还是指向node节点，并且是可达的状态，不会被回收，那是什么时候取消prev的指向的呢？**
> **在别的线程调用**_**cancelAcquire**_()、或者_**shouldParkAfterFailedAcquire**_**方法时，会根据prev跳过被取消的前驱节点，同时会调整其遍历过的prev，以达到使node出队的目的，**_**2->3  **_**代码如下**
> Node pred = node.prev;
> while (pred.waitStatus > 0)
> node.prev = pred = pred.prev;


<br />

<a name="munMn"></a>
### 场景三、node为head的后继节点
**指针变动与场景二一致，但是unparkSuccessor方法中没有对队列做任何操作，那么node节点是在什么时候出队对呢？**<br />出队操作实际上是由unparkSuccessor**唤醒的线程unparkT执行的，注意看线程unparkT唤醒后做的事情**<br />![untitled.jpg](https://cdn.nlark.com/yuque/0/2019/jpeg/261655/1570613290033-a5791633-f145-4960-b153-cfbb495f6f66.jpeg#align=left&display=inline&height=520&margin=%5Bobject%20Object%5D&name=untitled.jpg&originHeight=520&originWidth=750&size=71008&status=done&style=none&width=750)<br />

```java
for (;;) {
    final Node p = node.predecessor();
    if (p == head && tryAcquire(arg)) {
        setHead(node);
        p.next = null; // help GC
        failed = false;
        return interrupted;
    }
    if (shouldParkAfterFailedAcquire(p, node) &&
        parkAndCheckInterrupt()//当初是这个方法中挂起的)
        interrupted = true;
}
```

<br />**unparkT线程被唤醒后，继续自旋，唤醒的是离head最近的未被取消的节点，第一次循环，如果node的前驱节点不是head(****是因为他俩之间有些节点被取消了，还在队列中****)，则在**shouldParkAfterFailedAcquire()**方法中跳过被取消的前驱节点，这个时候，node的前驱节点就肯定是head了(****取消节点node就已经出队了，等待被JVM回收，****如果存在并发，别的node排在了head的后面，无所谓，还是被挂起，继续等待被唤醒****)，在下一次循环，node节点就会竞争同步资源，成功后，把node设为head**<br />**2->3的出队代码为**<br />

```java
do {
   //前驱节点被取消，往前遍历，直到找到未取消状态到节点
       node.prev = pred = pred.prev;
   } while (pred.waitStatus > 0);
// 当前节点放到该节点后面
pred.next = node;
```



---

<a name="ILdoG"></a>
# tryAcquireNanos -- 超时获取同步状态


```java
public final boolean tryAcquireNanos(int arg, long nanosTimeout)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    return tryAcquire(arg) ||
        doAcquireNanos(arg, nanosTimeout);
}

private boolean doAcquireNanos(int arg, long nanosTimeout)
        throws InterruptedException {
    if (nanosTimeout <= 0L)
        return false;
    // 超时时间
    final long deadline = System.nanoTime() + nanosTimeout;
    // 新增Node节点
    final Node node = addWaiter(Node.EXCLUSIVE);
    boolean failed = true;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return true;
            }
            // 获取同步状态失败，做超时、中断判断
            // 重新计算需要休眠的时间
            nanosTimeout = deadline - System.nanoTime();
            // 已超时直接返回false
            if (nanosTimeout <= 0L)
                return false;
            // 如果没有超时，等待nanosTimeout纳秒
            if (shouldParkAfterFailedAcquire(p, node) &&
                nanosTimeout > spinForTimeoutThreshold)
                LockSupport.parkNanos(this, nanosTimeout);
            if (Thread.interrupted())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

<br />tryAcquireNanos是acquireInterruptibly方法的增强，**除了响应中断，还有超时控制**。即如果没有在指定时间内获取到同步状态，则返回false，否则返回true。<br />如果休眠时间大于spinForTimeoutThreshold，则休眠等待，否则，不休眠，进行快速自旋，因为1000ns已经是非常小了，非常短的时间等待无法做到精准，所以在超时非常短的场景下，AQS会进行无条件的快速自旋
