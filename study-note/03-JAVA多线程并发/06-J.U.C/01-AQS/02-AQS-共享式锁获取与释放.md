# AQS-共享式锁获取&amp;释放

<a name="AAYp1"></a>
# acquireShared-获取同步状态


```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```
与独占式要做的事情是一样的，不同的是**tryAcquireShared**返回的是int类型：

  - 如果小于0，代表获取同步状态失败
  - 如果等于0，代表获取同步状态成功，但接下来其他线程获取同步状态会失败（没有可用资源了）
  - 如果大于0，代表获取同步状态成功，但接下来其他线程获取同步状态可能会成功（有可用资源）
<a name="5a7MK"></a>
## doAcquireShared


```java
private void doAcquireShared(int arg) {
    // 把当前线程包装成node节点，添加到同步队列队尾
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                // 当前节点的前驱节点为head，则再次尝试获取同步状态
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    // 获取同步状态成功
                    if (r >= 0) {
                        // 把当前节点设为head，如果有剩余资源可以再唤醒之后的线程
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                 //检查获取失败后是否可以阻塞
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
```

<br />该方法与独占式的**acquireQueued**要做的事情是一样的，逻辑上仅有两处不同

1. **`addWaiter(Node.EXCLUSIVE)`** ->** `addWaiter(Node.SHARED)`**
1. **`setHead(node)` **-> **`setHeadAndPropagate(node, r)`**

第一点的不同主要表现为Node的nextWaiter属性上，是起到了标志的作用，**`EXCLUSIVE为`**独占式**`SHARED`**为共享式<br />第二点区别就在于获取锁之后的行为上，独占式是直接调用了**`setHead(node)`**，而共享式调用了**`setHeadAndPropagate(node, r)`**，<br />

```java
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; // Record old head for check below
    setHead(node);
    /**
    * propagate > 0 说明还有资源可被其他线程获取
    * 或者head的waitStatus为PROPAGATE(PROPAGATE可以被转换为SIGNAL)说明需要往后传递
    * 或者为null，我们还不确定是什么情况
    * 上面的检查有点保守，在有多个线程竞争获取/释放的时候可能会导致不必要的唤醒
    */
    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        // 共享模式head可能已被修改，所以重新赋值
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        // 后继节点是共享模式或为null
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```
该方法不仅调用了**`setHead(node)`**，还在一定条件下调用了**`doReleaseShared()`**来唤醒后继节点。这是因为在共享模式下，锁是可以被多个线程共同持有的，既然当前线程已经获取到锁，那么就可以通知后继节点来获取锁，就不用等锁被释放的时候再去唤醒后继节点<br />**doReleaseShared的下面详解**
<a name="rwFSw"></a>
# releaseShared-释放同步状态


```java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        // 释放成功，唤醒后继节点
        doReleaseShared();
        return true;
    }
    return false;
}
```
<a name="29Yzs"></a>
# doReleaseShared


```java
private void doReleaseShared() {
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }
```

<br />该方法为共享锁能做到同时被多个线程持有的核心方法，下面我们来一一分析<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/261655/1571278522324-fd8b79e2-365a-4587-a2cb-97488029f45c.png#align=left&display=inline&height=68&name=image.png&originHeight=135&originWidth=932&size=182005&status=done&style=none&width=466)

- 1) 该方法有几处调用？ 

首先该方法有两处调用，一处是**`doAcquireShared->setHeadAndPropagate`**方法中，当线程获取到共享锁后，在一定条件下调用；另一处是在**`releaseShared`**方法中，当线程释放共享锁的时候调用

- 2）调用该方法的线程是谁？

在独占锁中，只有获取到锁的线程才能调用release释放锁，因此调用unparkSuccessor方法的一定是持有锁的线程，该线程可看做是当前的头节点<br />
<br />在共享锁中，持有共享锁的会有多个线程，这些线程都可以调用**`releaseShared`**方法释放锁，而这些线程想释放锁，则他们**必然曾经是头节点，或者现在就是**，因此在**`releaseShared`**方法中调用**`doReleaseShared`**，可能此时调用该方法的线程已经不是头节点，头节点可能已经易主了好几次

- 3）调用该方法的目的是什么？

该方法的目的是在共享锁可获取的状态下，**唤醒head节点的下一个节点**，当头节点变化的时候，立刻触发**唤醒head的下一个节点**操作，如此周而复始

- 4）退出条件是什么？

该方法是个for(;;)自旋方法，退出的唯一方法是最后的**`break`**，满足break的条件是**`h == head`**，即头节点没有易主，接下来我们看一下这个方法并发执行的情况<br />

> Head->A->B->C->D

我们假设队列中有以上节点，接下来A拿到了共享锁
> Head(A)->B->C->D

此时A获取到了共享锁，变成了头节点，然后A线程执行**`doReleaseShared`**方法记为**`doReleaseShared[A]，`**唤醒后继节点B，接下来，线程B竞争锁，会存在两种情况：<br />
<br />1、B还未获取到共享锁，**`doReleaseShared[A]`**执行到**`if (h == head)`**，此时，head没有易主，则**`doReleaseShared[A]`**运行结束；等B获取到共享锁后，在执行**`doReleaseShared`**方法，唤醒后继节点<br />2、B获取到共享锁，`**doReleaseShared[A]**`还未执行完，那么此时线程B也开始执行doReleaseShared方法，记为**`doReleaseShared[B]`**,
> Head(B)->C->D

如果是上面的第二种情况，此时A、B线程都在执行**`doReleaseShared`**方法，当A线程执行到**`if (h == head)`**时，因为**head已经易主**，A不会退出该方法，会**立即触发唤醒head后继节点(C)**的操作；而`**doReleaseShared[B]**`也在唤醒C，又因为**`compareAndSetWaitStatus(h, Node.SIGNAL, 0)`**操作保证了，只能有一次成功；多个线程都在做唤醒C操作，大大的提高来唤醒效率，又能保证线程安全。<br />
<br />**明确上面的几个问题后，我们来分析这个方法中具体怎么做的**<br />
<br />1.首先是进入到for(;;)自旋操作，第一步先对同步队列进行判断<br />

```java
if (h != null && h != tail){
    ...
}
```
只有当头节点不为空，且头节点不等于尾节点，才进入下一步操作，这是什么意思呢？<br />头节点不为空，好理解，即队列中至少有一个节点<br />头节点不等于尾节点，就是说，除了头节点以外，至少还有一个节点，**即至少有两个节点**！才会进入到下一步<br />
<br />2.接下来进入到来两个if语句<br />

```java
int ws = h.waitStatus;
if (ws == Node.SIGNAL) {
    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
        continue;            // loop to recheck cases
	unparkSuccessor(h);
}
else if (ws == 0 &&
		!compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
	continue;
```
第一个if是，如果头节点状态为SIGNAL，说明后继节点挂起或准备挂起，那么，先CAS把头节点状态设为0，然后执行`**unparkSuccessor(h)**`**_唤醒距head最近的未被取消状态的节点_**<br />
<br />第二个if比较难理解，首先是判断ws==0，我们想一下，**ws什么时候会为0**？一种是上面到情况，但是显然不会再走到**`else if`**里，排除，还有一种**是节点刚入队的时候，ws也是0**，因为有前置条件，**队列至少有两个节点**<br />下面来分析一下，假设一开始队列中有两个节点，**head和A，此时A的状态肯定是0**，如果接下来有**B入队**，然后**A获取到共享锁**，变成了head，那么此时，队列中有两个节点，且**`head(A)`**和B的状态都为0，那么就满足了else if的前半部分条件，就会接着执行`**compareAndSetWaitStatus(h, 0, Node.PROPAGATE)**`<br />`**compareAndSetWaitStatus(h, 0, Node.PROPAGATE)**`方法是个CAS方法，首先看下如果执行成功了，会发生什么，**`continue`**不会执行，逻辑继续往下执行，根据判断head是否易主，判断是否退出<br />如果**`compareAndSetWaitStatus(h, 0, Node.PROPAGATE)`**执行失败了，那么什么时候回执行失败？说明我们执行这个方法时，h的ws不是0了！<br />**那么为什么不是0？谁把h的ws修改了？**<br />记得在之前分析获取同步状态的时候，有个方法是`**shouldParkAfterFailedAcquire**`，该方法会把当前节点挂起，并把前置节点的ws设为`**SIGNAL**`，所以，明确了，B获取同步状态时，把A的ws设为了SIGNAL，所以**`compareAndSetWaitStatus(h, 0, Node.PROPAGATE)`**执行失败，然后执行`**continue**`，再次自旋，重复上面的操作<br />
<br />![untitled.jpg](https://cdn.nlark.com/yuque/0/2019/jpeg/261655/1571625509273-3a7c140d-f835-4f04-8c08-36ff45f50007.jpeg#align=left&display=inline&height=940&name=untitled.jpg&originHeight=940&originWidth=706&size=71008&status=done&style=none&width=706)
