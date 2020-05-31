# CAS和Unsafe

<a name="zxQzF"></a>
## CAS
**compare and swap比较并替换，CAS有三个参数：需要读写的内存位值（内存偏移地址 V），预期的原值（A），要写入的新值（B）；当且仅当V的值等于A时，CAS才会通过原子操作用新值B来更新V的值，否则不会做任何操作，** **CAS会返回更新之前的值**

```java
public static void main(String[] args) {
    AtomicInteger integer = new AtomicInteger(0);   // V的值为0
    System.out.println(integer.getAndIncrement());  // 打印 0 V的值为1
    System.out.println(integer.get());				// 打印 1
}
```

<a name="eTkPz"></a>
### 缺陷
<a name="pyIW0"></a>
#### 循环时间太长

- 如果CAS一直不成功，则一直循环，造成CPU开销很大。在JUC中有些地方就限制了CAS的自旋次数
<a name="orDYn"></a>
#### 只能保证一个共享变量的原子操作

- 看CAS的实现得知是针对一个共享变量，如果是多个共享变量就只能使用锁，如果可以把多个变量变成一个变量（AtomicReference），也可以使用CAS
<a name="sh6hE"></a>
#### ABA问题

- 线程a执行A->B->A
- 线程b执行A->C
- 在CAS检查时，线程b发现A的值没有变，允许更新，实际上是已经更新了
- 操作链表等
<a name="Ntrd6"></a>
#### 解决方案

- 加版本号，JUC中AtomicStampedReference，将更新一个“对象-stamp”二元组，两个变量会原子更新

```java
@Test
public void test1() {
   	Person person = new Person(1L, "bob", 12);
   	AtomicStampedReference<Person> reference = new AtomicStampedReference<>(person, 1);
	boolean a = reference.compareAndSet(person, new Person(2L, "mom", 12), 2, 1);
    //a=false
	boolean b = reference.compareAndSet(person, new Person(3L, "mom", 12), reference.getStamp(), 1);
	// b=true
	reference.getStamp()+","+reference.getReference();
    //reference 1,Person{id=3, name='mom', age=12}
}
```

<a name="dqRr2"></a>
## Unsafe

```java
public static void main(String[] args) throws NoSuchFieldException {
        Integer i = 1;
        Unsafe unsafe = getUnsafe();//获取unsafe
   		//获取value在Integer对象中的内存偏移地址
        long fieldOffset = unsafe.objectFieldOffset
                (Integer.class.getDeclaredField("value"));
 
//i:需要变更的对象，fieldOffset:变更的值在变更对象中的内存偏移地址，1:预期原值，2:要写入的新值
        unsafe.compareAndSwapInt(i, fieldOffset, 1, 2); 
        System.out.println(unsafe.getAndAddInt(i, fieldOffset, 2));// 2
  			System.out.println(i);// 4
}
// Unsafe对象实例化方法
   private static Unsafe getUnsafe() {
        Unsafe unsafe = null;
        try {
            Class<?> clazz = Unsafe.class;
            Field f;

            f = clazz.getDeclaredField("theUnsafe");

            f.setAccessible(true);
            unsafe = (Unsafe) f.get(clazz);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return unsafe;
    }
```

[下一篇：02-Thread.md](02-Thread.md)