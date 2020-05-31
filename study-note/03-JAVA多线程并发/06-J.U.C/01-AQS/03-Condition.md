# Condition

<a name="LgdWb"></a>
# 实现原理
<a name="i2W9Y"></a>
### 说明
Condition为JUC提供了等待/通知方法，类似与监视器锁的Object.wait和Object.notify方法；在使用JUC框架中的锁时，线程在调用awit方法后执行线程挂起，直到线程等待的某个条件成立才会被唤醒 ，Condition必须配合锁一起使用，因为对共享变量的访问发生在多线程环境。一个Condition的实例必须与一个Lock绑定，因此Condition一般都是作为Lock的内部类。<br />

<a name="8RPFL"></a>
### 原理
在Condition内部存在一个条件队列，调用await的方法，会把线程包装成一个节点(Node)，添加到条件队列中然后自旋等待或挂起；调用signal方法，该线程的节点会从条件队列出队，然后添加到同步队列中，尝试获取锁
> **说明：AQS中的同步队列，为双向链表，node中prev代表前置节点，next代表后继节点；Condition中的条件队列为单向链表，node中nextWaiter代表后继节点<br />**



<a name="pY9MP"></a>
### 以下是AQS队列和Condition队列的出入节点的示意图

- 1.**初始化状态：**AQS同步队列有3个节点，head节点线程为T1，Condition条件队列有1个节点(也可以没有)

![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1587549872025-9f03235d-fb7b-4bf4-bc0f-436bd9780866.png#align=left&display=inline&height=492&margin=%5Bobject%20Object%5D&name=image.png&originHeight=492&originWidth=640&size=77268&status=done&style=none&width=640)<br />

- 2.**节点1执行await**：
  - 节点1添加到条件队列队尾，设为lastWaiter
  - T1释放锁（T2竞争到锁，setHead为T2，使节点1出队）


<br />
<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1587549884084-fba4472e-4be2-4780-9952-dded988b8557.png#align=left&display=inline&height=361&margin=%5Bobject%20Object%5D&name=image.png&originHeight=361&originWidth=640&size=88231&status=done&style=none&width=640)

- 3.**节点2执行signal**
  - 将firstWaiter设为firstWaiter.next
  - 将老到firstWaiter节点4从条件队列出队
  - 将节点4添加到同步队列
  - 设置tail为节点4


<br />
<br />
<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1587549893051-d9c77619-e5fa-4652-aa8d-a7fa9276727f.png#align=left&display=inline&height=356&margin=%5Bobject%20Object%5D&name=image.png&originHeight=356&originWidth=640&size=93641&status=done&style=none&width=640)<br />

<a name="DKFSM"></a>
# 源码分析
<a name="1rfsR"></a>
## await
```java
public final void await() throws InterruptedException {
    // 1.如果当前线程被中断，则抛出中断异常
    if (Thread.interrupted())
        throw new InterruptedException();
    // 2.将节点加入到条件队列
    Node node = addConditionWaiter();
    // 3.调用tryRelease,释放当前线程的锁
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    // 4.判断当前线程是否在同步队列
    // 	a.如果当前node在同步队列中，说明已有其他线程执行signal把当前节点从条件队列出队并添加到
    //    同步队列中，这时当前线程就不需要自旋等待
    //	b.如果当前node不在同步队列中，说明当前节点还没有被signal，需要挂起当前线程
    while (!isOnSyncQueue(node)) {
        LockSupport.park(this);//线程在此处被挂起
        // 5.当前线程被唤醒，或者被中断导致唤醒，
        // 检测是否中断过，是则退出循环，不是则是被唤醒，节点在同步队列，也退出循环
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    // 6.此时线程已被唤醒，自旋尝试获取锁
    //   无论是线程被唤醒，还是中断，执行到这里，节点肯定在同步队列中
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        // 竞争锁时被中断过，并且signal前没有被中断
        interruptMode = REINTERRUPT;
    // 7.如果是通过signal唤醒，则node.nextWaiter==null，
    //   如果是被中断后唤醒，node.nextWaiter!=null(只是把node的状态置为0然后 enq)
    if (node.nextWaiter != null) // clean up if cancelled
        // 当前节点清理出队
        unlinkCancelledWaiters();
    // 8.如果线程中断过，做相应操作
    //   在signal之前中断，抛出异常，signal之后中断，自我中断，设置个中断标志
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
}
```

- 1.将当前线程包装成Node节点，添加到条件队列中
- 2.**释放当前持有的锁**
- 3.自旋挂起，直到被唤醒或者被中断，
- 4.唤醒后，竞争锁
- 5.如果当前节点是中断被唤醒，则后继节点不为空，则清理条件队列中被取消到节点
- 6.如果被中断过，进行相应到处理（抛出中断异常/自我中断一下，设置个中断状态）
<a name="BtSSE"></a>
### addCondition
**把当前线程包装成节点，添加到条件队列**
```java
private Node addConditionWaiter() {
    Node t = lastWaiter;
    // If lastWaiter is cancelled, clean out.
    // 如果最后一个等待节点不是等待状态，则清理条件队列中非等待状态的节点
    if (t != null && t.waitStatus != Node.CONDITION) {
        unlinkCancelledWaiters();
        // 获取到新的尾节点
        t = lastWaiter;
    }
    // 当前线程包装成新节点，状态为CONDITION
    Node node = new Node(Thread.currentThread(), Node.CONDITION);
    if (t == null)
        firstWaiter = node;
    else
        t.nextWaiter = node;
    // 设置当前节点为条件队列尾节点
    lastWaiter = node;
    return node;
}
```

- 清理条件队列中非等待状态的节点
- 把当前线程包装成Node节点，添加到条件队列队尾



<a name="HUHus"></a>
### fullRerlease
**释放当前节点持有的锁**
```java
final int fullyRelease(Node node) {
    boolean failed = true;
    try {
        int savedState = getState();
        // 调用AQS的release方法释放锁
        if (release(savedState)) {
            failed = false;
            return savedState;
        } else {
            // 释放失败 抛出异常 当前线程未持有锁
            throw new IllegalMonitorStateException();
        }
    } finally {
        // 如果释放锁异常，把当前节点的状态置为取消
        if (failed)
            node.waitStatus = Node.CANCELLED;
    }
}
```

- 调用AQS的release方法释放锁
- 如果释放锁异常，把当前节点取消
<a name="5M0sH"></a>
### isOnSyncQueue
**判断当前节点是否在同步队列中**<br />**节点如果****同步队列中****，要么从条件队列出队！！，要么在条件队列中状态不是等待状态！！**
```java
final boolean isOnSyncQueue(Node node) {
    // 如果节点状态为CONDITION，则代表节点在条件队列中
    // 节点到的置节点为空，则当前节点不在同步队列(同步队列中节点都有前置节点，最前端的前置节点为head)
    if (node.waitStatus == Node.CONDITION || node.prev == null)
        return false;
    // 节点的后继节点不为null，则肯定在同步队列中
    if (node.next != null) // If has successor, it must be on queue
        return true;


    /* 能执行到这里，说明node长在从条件队列出队，node.waitStatus!=CONDITION
     *							并且 node.prev!=null 但是node.next==null，
     * 为什么会这样呢？
     * enq方法在把node添加到同步队列： 1.把当前节点的前置节点设为同步队列的尾节点，
     *							  2.CAS设置当前节点为同步队列尾节点
     *							  3.设置老的尾节点的后继节点为当前节点
     * 由于node节点入队不是一个原子操作，CAS设置尾节点可能失败，
     * 所以需要从尾节点遍历队列判断节点是否在同步队列中
     */
    return findNodeFromTail(node);
}

/**
 * 从同步队列尾节点遍历队列，判断节点node是否在同步队列中
 * @return true if present
 */

private boolean findNodeFromTail(Node node) {
    Node t = tail;
    for (;;) {
        if (t == node)
            return true;
        if (t == null)
            return false;
        t = t.prev;
    }
}
```

- <br />
<a name="Zw1XH"></a>
### checkInterruptWhileWaiting
**检测线程是否中断**
```java
private int checkInterruptWhileWaiting(Node node) {
    return Thread.interrupted() ?
        /* 如果被中断，判断是何时在中断
         * 如果是signal之前中断，返回THROW_IE
         * 如果在signal之后中断，返回REINTERRUPT
         */
        (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
        0;
}

```
<a name="Qxwvg"></a>
### transferAfterCancelledWait
**退出等待后，把节点转移到同步队列**
```java
final boolean transferAfterCancelledWait(Node node) {
    // CAS设置当前节点的状态为0
    if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
        // 状态设置成功，说明当前节点是在条件队列中，
        // 把当前节点添加到同步队列
        enq(node);
        return true;
    }

    /* 执行到这里，说明CAS失败，node.waitStatus!=CONDITION,说明该节点已被signal，
     * 状态已被修改，此时判断节点是否在同步队列中，如果不在，说明enq还未完成，节点还未入队同步队列
     * 只需要自旋等待即可， 因为这会很短
     */
    while (!isOnSyncQueue(node))
        Thread.yield();
    return false;
}
```
<a name="EjdJE"></a>
### unlinkCancelledWaiters
**清理条件队列中非等待状态的节点**<br />**从firstWaiter开始遍历，清除队列中所有waitStatus != Node.CONDITION的节点**<br />**(主要清理 ****中断唤醒的节点状态为0，释放锁异常的状态为1****)**
```java
private void unlinkCancelledWaiters() {
    Node t = firstWaiter;
    Node trail = null;
    while (t != null) {
        Node next = t.nextWaiter;
        if (t.waitStatus != Node.CONDITION) {
            /*
             * 当前节点的状态不是CONDITION，则需要出队，
             * 当前节点的nextWaiter设为null
             * 如果trail为null(还没遍历到状态是CONDITION的节点)设置当前节点的后继节点为firstWaiter
             * 否则，把当前节点的后继设置为trail的后继节点(当前节点完成出队)
             */
            t.nextWaiter = null;
            if (trail == null)
                firstWaiter = next;
            else
                trail.nextWaiter = next;
            // 后面没有节点了，设置trail为lastWaiter
            //  (此时trail为最后一个状态为CONDITION的节点)
            if (next == null)
                lastWaiter = trail;
        }
        else
            // 如果当前节点的状态是CONDITION，记录为trail
            trail = t;
        // 指向下一个节点
        t = next;
    }
}
```

<br />1.进入await()时必须是已经持有了锁<br />2.离开await()时同样必须是已经持有了锁<br />3.调用await()会使得当前线程被封装成Node扔进条件队列，然后释放所持有的锁<br />4.释放锁后，当前线程将在condition queue中被挂起，等待signal或者中断<br />5.线程被唤醒后会将会离开condition queue进入sync queue中进行抢锁<br />6.若在线程抢到锁之前发生过中断，则根据中断发生在signal之前还是之后记录中断模式<br />7.线程在抢到锁后进行善后工作（离开condition queue, 处理中断异常）<br />8.线程已经持有了锁，从await()方法返回
<a name="CxHZE"></a>
## ![image.png](https://cdn.nlark.com/yuque/0/2020/png/261655/1587719252379-c309b569-208b-4359-8fd3-0ff5f218223b.png#align=left&display=inline&height=180&margin=%5Bobject%20Object%5D&name=image.png&originHeight=180&originWidth=800&size=121315&status=done&style=none&width=800)
在这一过程中我们尤其要关注中断，如前面所说，**中断和signal所起到的作用都是将线程从`condition queue`中移除，加入到`sync queue`中去争锁，所不同的是，signal方法被认为是正常唤醒线程，中断方法被认为是非正常唤醒线程，如果中断发生在signal之前，则我们在最终返回时，应当抛出`InterruptedException`；如果中断发生在signal之后，我们就认为线程本身已经被正常唤醒了，这个中断来的太晚了，我们直接忽略它，并在`await()`返回时再自我中断一下，这种做法相当于将中断推迟至`await()`返回时再发生。**
<a name="T3WjY"></a>
## signal
```java
public final void signal() {
    // 当前线程是否是持有锁的线程
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    Node first = firstWaiter;
    // 条件队列不为空
    if (first != null)
        // 从条件队列第一个节点signal
        doSignal(first);
}
```


<a name="rFdfD"></a>
### doSignal
**从条件队列首节点开始唤醒一个状态为CONDITION的节点**
```java
private void doSignal(Node first) {
    do {
        // 设置条件队列的首节点后继节点为首节点
        if ( (firstWaiter = first.nextWaiter) == null)
            // 如果条件队列没有节点，则清空
            lastWaiter = null;
        // 节点出队
        first.nextWaiter = null;
    } while (!transferForSignal(first) &&
             // 唤醒节点失败，循环唤醒下个节点
             (first = firstWaiter) != null);
}
```


<a name="DUWRp"></a>
### transferForSignal
**将节点从条件队列转移到同步队列**
```java
final boolean transferForSignal(Node node) {
    /*
     * 如果CAS失败，说明该节点状态已被修改，节点已被取消
     */
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        return false;

    /*
     * Splice onto queue and try to set waitStatus of predecessor to
     * indicate that thread is (probably) waiting. If cancelled or
     * attempt to set waitStatus fails, wake up to resync (in which
     * case the waitStatus can be transiently and harmlessly wrong).
     */
    // 节点添加到同步队列
    Node p = enq(node);
    int ws = p.waitStatus;
    // 将前置节点到状态设为SIGNAL，以便唤醒node，如果设置失败，直接唤醒节点线程，尝试竞争锁
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        LockSupport.unpark(node.thread);
    return true;
}
```
**唤醒条件队列节点步骤**

- 把节点状态CAS设置为0
- enq方法把节点添加到同步队列（**不是原子性操作**）
  - 先把节点的前置节点指向tail
  - CAS把当前节点设置未tail
  - old tail的后继节点指向当前节点
- 把节点的前置节点的状态设为SIGNAL，设置失败则直接唤醒当前线程
<a name="RmHMe"></a>
## signalAll
```java
public final void signalAll() {
    if (!isHeldExclusively())
        throw new IllegalMonitorStateException();
    Node first = firstWaiter;
    if (first != null)
        // 把条件队列所有节点signal
        doSignalAll(first);
}
```
<a name="abUxZ"></a>
### doSignalAll
**把条件队列所有节点出队，并添加到同步队列**
```java
private void doSignalAll(Node first) {
    // 清空条件队列
    lastWaiter = firstWaiter = null;
    do {
        // 节点出队
        Node next = first.nextWaiter;
        first.nextWaiter = null;
        // 节点添加到同步队列
        transferForSignal(first);
        // 指向下一个节点
        first = next;
    } while (first != null);
}
```
<a name="IeUT5"></a>
## awaitUninterruptibly
**不中断的等待，只有被signal才会退出等待，重新竞争锁**
```java
public final void awaitUninterruptibly() {
    Node node = addConditionWaiter();
    int savedState = fullyRelease(node);
    boolean interrupted = false;
    while (!isOnSyncQueue(node)) {
        LockSupport.park(this);// 挂起
        // 如果线程被中断，标记一下
        if (Thread.interrupted())
            interrupted = true;
    }
    if (acquireQueued(node, savedState) || interrupted)
        // 竞争锁过程中被中断 || 等待过程中被中断，自我中断一下，
        selfInterrupt();
}
```
**awaitUninterruptibly方法与await不同的点**

- awaitUninterruptibly不会因为被中断而退出自旋等待，awaitUninterruptibly退出自旋等待的条件是必须被signal
- awaitUninterruptibly竞争锁中被中断或者等待过程中被中断，都是仅自我中断一下，不会抛出异常



<a name="fgO9h"></a>
## awaitNanos(long nanosTimeout)
**带有超时机制的等待，超过等待时间，退出等待，重新竞争锁**
```java
public final long awaitNanos(long nanosTimeout)
        throws InterruptedException {
    /*if (Thread.interrupted())
        throw new InterruptedException();
    Node node = addConditionWaiter();
    int savedState = fullyRelease(node);
    final long deadline = System.nanoTime() + nanosTimeout;
    int interruptMode = 0;
    while (!isOnSyncQueue(node))*/ {
        // 与await不同点
        // 超出等待时间，直接把节点从条件队列移到同步队列，退出自旋等待
        if (nanosTimeout <= 0L) {
            transferAfterCancelledWait(node);
            break;
        }
        // 剩余等待时间大大于阀值，挂起等待，否则自旋等待
        if (nanosTimeout >= spinForTimeoutThreshold)
            LockSupport.parkNanos(this, nanosTimeout);
        /*if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;*/
        // 与await不同点
        nanosTimeout = deadline - System.nanoTime();
    }
    /*if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null)
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);*/
    // 与await不同点
    // 返回剩余等待时间
    return deadline - System.nanoTime();
}
```
**与await不同点**

- 每次循环记录剩余等待时间，当剩余等待时间<=0时，把节点从条件队列移到同步队列
- 剩余等待时间大于阀值，挂起一定时间等待，否则，自旋等待，减少挂起线程和唤醒线程带来到时间消耗
- 返回剩余等待时间，返回值大于0，说明超时时间还没到，该返回是由signal()导致



<a name="U9zcA"></a>
## await(long time, TimeUnit unit)


```java
public final boolean await(long time, TimeUnit unit)
        throws InterruptedException {
    // 与awaitNanos不同
    long nanosTimeout = unit.toNanos(time);
    /*if (Thread.interrupted())
        throw new InterruptedException();
    Node node = addConditionWaiter();
    int savedState = fullyRelease(node);
    final long deadline = System.nanoTime() + nanosTimeout;
    boolean timedout = false;
    int interruptMode = 0;
    while (!isOnSyncQueue(node)) {
        if (nanosTimeout <= 0L) {*/
    		// 与awaitNanos不同
            timedout = transferAfterCancelledWait(node);
            /*break;
        }
        if (nanosTimeout >= spinForTimeoutThreshold)
            LockSupport.parkNanos(this, nanosTimeout);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
        nanosTimeout = deadline - System.nanoTime();
    }
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null)
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);*/
    // 与awaitNanos不同
    return !timedout;
}
```
**与awaitNanos不同**

- 入参可以指定时间单位，await(long time, TimeUnit unit)中把时间转换成纳秒，和**awaitNanos一样计算时间**
- 返回**transferAfterCancelledWait**的返回结果，**还未signal返回true**，**已经signal返回false**
- await**返回true代表在被signal返回，false代表是超时等待返回**
- **调用await(long time, TimeUnit unit)等价与调用awaitNanos(time)>0**

**
<a name="7hEIE"></a>
## await(Date deadline)
**指定超时时间绝对值的超时等待**
```java
public final boolean awaitUntil(Date deadline)
        throws InterruptedException {
    long abstime = deadline.getTime();
    if (Thread.interrupted())
        throw new InterruptedException();
    Node node = addConditionWaiter();
    int savedState = fullyRelease(node);
    boolean timedout = false;
    int interruptMode = 0;
    while (!isOnSyncQueue(node)) {
        if (System.currentTimeMillis() > abstime) {
            timedout = transferAfterCancelledWait(node);
            break;
        }
        LockSupport.parkUntil(this, abstime);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
        interruptMode = REINTERRUPT;
    if (node.nextWaiter != null)
        unlinkCancelledWaiters();
    if (interruptMode != 0)
        reportInterruptAfterWait(interruptMode);
    return !timedout;
}
```
与await(long nanosTimeout)和await(long time, TimeUnit unit)代码基本一致，入参的deadline，等价与await(long nanosTimeout)和await(long time, TimeUnit unit)中的**deadline**，代表着到哪个时间点超时；还有就是这个方法中没有使用_spinForTimeoutThreshold_进行自旋优化，一般使用该方法都是较长时间的等待<br />
<br />
<br />
<br />参考：<br />[https://segmentfault.com/a/1190000016462281](https://segmentfault.com/a/1190000016462281)<br />[https://juejin.im/post/5def445af265da33c4280639#heading-4](https://juejin.im/post/5def445af265da33c4280639#heading-4)<br />[https://blog.csdn.net/itcats_cn/article/details/81280893](https://blog.csdn.net/itcats_cn/article/details/81280893)


[下一篇：JUC锁](../02-JUC锁/00-JUC锁.md)