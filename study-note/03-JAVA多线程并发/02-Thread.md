# Thread

<a name="xAi3E"></a>
## 一、基本方法
- NEW(新建)：使用 new 关键字创建了一个线程之后，该线程就处于新建状态，此时仅由 JVM 为其分配
内存，并初始化其成员变量的值
- RUNNABLE(就绪)：当线程对象调用了 start()方法之后，该线程处于就绪状态。Java 虚拟机会为其创建方法调用栈和程序计数器，等待调度运行
- RUNNING(运行)：如果处于就绪状态的线程获得了 CPU，开始执行 run()方法的线程执行体，则该线程处于运行状态
- BLOCKED(阻塞)：阻塞状态是指线程因为某种原因放弃了 cpu 使用权，也即让出了 cpu timeslice，暂时停止运行
  - WAATING(等待)：
  - TIME-WAITING(超时等待)：
- DEAD(终止)：线程结束




| 方法 | 作用 | 执行后线程状态 | 是否释放锁 |
| --- | --- | --- | --- |
| Thread.start() | 线程启动 | NEW->RUNNABLE |  |
| Object.wait() | 线程等待 | RUNNING->WAITING | 释放锁 |
| Thread.sleep(time) | 线程睡眠 | RUNNING->TIMED-WAITING | 不释放锁 |
| Thread.yield() | 线程让步，使当前线程让出CPU执行时间片 | RUNNING->RUNNABLE | 释放锁 |
| Thread.interrupt() | 线程中断，给线程一个通知信号，会影响这个线程内部的中断标示位，线程并不会立刻中断正在运行的线程。<br />在线程的 run 方法内部可以根据thread.isInterrupted()的值来优雅的终止线程 | 线程状态不会立刻改变 |  |
| Object.join(Thread) | 当前线程转为阻塞状态，另一个线程结束后，当前线程由阻塞->就绪，重新竞争CPU时间片 | RUNNING-BLOCKED->RUNNABLE | 释放锁 |
| Object.notify() | 唤醒在此对象监视器上等待的单个线程 | BLOCKED-RUNNABLE |  |
| Object.notifyAll() | 唤醒在此监视器上等待的所有线程 	 | BLOCKED-RUNNABLE |  |



<a name="lQg2G"></a>
## 二、线程上下文切换

- 巧妙的利用了时间片轮转的方式，CPU给每个任务都服务一定的时间，然后把当前任务的状态保存下来，在加载下一任务的状态后，继续服务下一任务，任务的状态保存及在加载，这段过程就叫做上下文切换。时间片轮转的方式使多个任务在同一个CPU上执行变成了可能。
