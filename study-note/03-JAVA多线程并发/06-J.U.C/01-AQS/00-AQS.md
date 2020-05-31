# AQS

<a name="sKanL"></a>
# 简介

---


<br />AQS(AbstractQueuedSynchronizer 队列同步器)是juc包中的并发工具基础，底层采用基于CAS的乐观锁，并在冲突的时候使用自旋的方式，实现轻量级和高效的获取锁。<br />
<br />AQS虽然被设计为抽象类，但是所有的方法都不是抽象方法，因为AQS设计出来是要支持多种用途的，如果被设计成抽象方法，那子类在继承时就需要实现不必要的方法。所以AQS将一些需要被覆盖的方法设计为project，将默认实现抛出UnsupportedOperationException异常，子类需要的时候重写该方法即可。<br />
<br />AQS中实现了锁的获取框架，采用模版方法模式，子类继承AQS类，把实际的锁获取逻辑在子类实现。就获取锁而言，子类必须实现tryAcquire方法。<br />
<br />AQS支持独占锁(Exclusive)和共享锁(Share)两种模式:

  - 独占锁：只能被一个线程获取到（ReantrantLock）
  - 共享锁：可以被多个线程同时获取（CountDownLatch，Semaphone，ReadWriteLock的读锁）



<a name="4MoQi"></a>
### 状态

<br />AQS使用一个int类型的成员变量state来代表同步的状态。state>0表示已经获取到锁，state = 0表示锁空闲。它提供了三个方法（getState()，setState(int newState)，compareAndSetState(int expact,int update)）来对同步状态操作，确保状态对所有线程可见(全局共享)，设置为volatile，并且AQS确保对其操作是线程安全的(锁竞争时期，采用compareAndSetState方法赋值，获取到锁以后，采用setState方法赋值，因为已经获取到锁，不存在竞争关系了)<br />

```java
private volatile int state;
```

<br />在独占模式中获取到锁后通过 setExclusiveOwnerThread(Thread thread) 方法把持有线程者设为当前线程，该变量exclusiveOwnerThread继承自AbstractOwnableSynchronizer<br />

```java
/**The current owner of exclusive mode synchronization.
	独占模式同步锁的当前所有者。**/
private transient Thread exclusiveOwnerThread;
```


<a name="mZQVt"></a>
### 队列


AQS通过一个内置的FIFO同步队列来完成资源获取线程的排队工作，它是一个双向链表，表示所有等待锁的线程的集合，如果当前线程获取同步状态失败，AQS会把当前线程及等待状态等信息包装成一个节点(Node)，并将其加入到阻塞队列中，同时阻塞当前队列，当同步状态释放时，会把当前节点唤醒，使其再次尝试获取同步状态。<br />

```java
static final class Node {
        /** 共享模式 */
        static final Node SHARED = new Node();
        /** 独占模式 */
        static final Node EXCLUSIVE = null;

    	// 节点等待状态 start
        /** 因为超时或中断，节点被设置为取消状态，取消状态的节点不会参与到竞争中 */
        static final int CANCELLED =  1;
        /**后继节点处于等待中，当前节点释放同步状态或被取消，将会通知后继节点，
        *  使后继节点的线程可以运行 */
        static final int SIGNAL    = -1;
        /**节点在等待队列中，节点线程等待在Condition上，当其他线程对Condition调用了signal()后，
        *  该节点将会从等待队列中转移到同步队列中，加入到同步状态的获取中  
   		*	共享模式中
        */
        static final int CONDITION = -2;
        /**
         * 表示下一次共享式同步状态获取将会无条件地传播下去
         *	共享模式中
         */
        static final int PROPAGATE = -3;
        // 节点等待状态end 以上四个静态常量值代表NODE节点的等待状态，后面遇到的时候会详细解释
    
    	/**
    	 * 等待状态
    	 */
        volatile int waitStatus;
        /**
         * 前驱节点
         */
        volatile Node prev;
        /**
         * 后继节点
         */
    	volatile Node next;
        /**
         * 获取同步状态的线程
         */
        volatile Thread thread;
        /**
         * 该属性用于代表条件队列或者共享锁
         */
        Node nextWaiter;

        /**
         * Returns true if node is waiting in shared mode.
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 获取前驱节点
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }
        Node() {    // Used to establish initial head or SHARED marker
        }
        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }
        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
```


<a name="1Y3oW"></a>
### CAS


```java
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                (Node.class.getDeclaredField("next"));

        } catch (Exception ex) { throw new Error(ex); }
    }
```

<br />CAS主要针对5个属性，AQS的3个，state，head，tail，以及Node中2个，waiteState，next，说明者5个属性会被多线程访问，<br />CAS的具体操作是调用Unsafe中的方法，如果失败就自旋<br />

<a name="o8hBV"></a>
# 主要方法



---



- **getState()**：返回同步状态的当前值
- **setState(int newState)**：设置当前同步状态值
- **compareAndSetState(int expect, int update)**：使用CAS设置当前状态，可以保证状态设置的原子性
- **tryAcquire(int arg)**：独占式获取同步锁，获取同步状态后，其他线程需要等待当前线程释放同步状态，才能获取同步状态，具体实现由自定义的队列同步器实现
- **tryRelease(int arg)**：独占式释放同步状态，具体实现由自定义的队列同步器实现
- **tryAcquireShare(int arg)**：共享式获取同步状态，返回值大于0表示获取成功，否则失败，具体实现由自定义的队列同步器实现
- **tryReleaseShare(int arg)**：共享式释放同步状态，具体实现由自定义的队列同步器实现
- **isHeldExclusively()**：当前同步器是否被当前线程独占，具体实现由自定义的队列同步器实现
- **acquire(int arg)**：独占式获取同步状态，如果成功则返回，否则，把当前线程包装成节点放到同步队列中等待，该方法会调用子类重写的tryAcquire方法
- **acquireInterruptibly(int arg)**：与acquire(int arg)方法相同，但是该方法响应中断，当前线程进入等待队列，如果线程中断，则会抛出异常InterruptedException并返回。(acquire方法会标记中断，不会影响自旋，会在线程获取到同步状态之后进行中断)
- **tryAcquireNanos(int arg, long nanosTime)**：独占式超时获取同步状态，如果在nanosTime时间内没有获取到同步状态，则返回false，否则返回true
- **acquireShared(int arg)**：共享式获取同步状态，如果未获取到同步状态，则会进入同步队列等待，与独占式到区别为，同一时刻可以由多个线程获取同步状态
- **acquireSharedInterruptibly(int arg)**：共享式获取同步状态，响应中断
- **acquireSharedNanos(int arg, long nanosTime)**：共享式获取同步状态，增加超时限制
- **release(int arg)**：独占式释放同步状态，释放之后会唤醒同步队列中第一个等待节点
- **relaseShare(int arg)**：共享式释放同步锁



> **自定义同步器，继承AQS时需按需重写以下方法**
> **tryAcquire**
> **
tryRelease**
> **
tryAcquireShared**
> **
tryReleaseShared**
> **
isHeldExclusively**
> 上面每个方法默认实现都是throws UnsupportedOperationException，定义这些方法是使用AbstractQueuedSynchronizer的唯一途径。


<br />
<br />[**AQS-独占式锁获取&释放**](https://www.yuque.com/weijungu/sr03pz/fpv0p1)<br />[**AQS-共享式锁获取&释放**](https://www.yuque.com/weijungu/sr03pz/ob8ulg)


[下一篇：01-AQS-独占式锁获取与释放.md](01-AQS-独占式锁获取与释放.md)