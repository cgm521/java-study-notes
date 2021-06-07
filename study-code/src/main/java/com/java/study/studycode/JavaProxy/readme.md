# 0、公共方法
- 接口UserService

![输入图片说明](https://gitee.com/uploads/images/2018/0604/154956_8888db5b_1844628.png "屏幕截图.png")

- 实现类UserServiceImpl

![输入图片说明](https://gitee.com/uploads/images/2018/0604/155017_80231c55_1844628.png "屏幕截图.png")

# 一、静态代理
- 使用静态的前提是目标对象有实现一个接口，
- 创建代理类，实现与目标对象实现的同一个接口，有实例域为目标对象，构造函数参数为目标对象；
- 实现的目标对象的方法，用目标对象调用目标方法，在其上下加上自己想要做的事
- 静态代理类的实现.代码已经实现好了.
- 使用时，实例化代理类，传参为目标类，调用各个方法

![输入图片说明](https://gitee.com/uploads/images/2018/0604/154930_88d7e6be_1844628.png "屏幕截图.png")

 **总结： 静态代理对代码的侵入性比较强，针对每个目标对象都实现代理类，每个方法都需要实现，如需要实现很多代理类，那么工作量会很大** 

# 二、动态代理（jdk代理）




- JDK实现代理只需要使用Proxy.newProxyInstance方法，但是该方法需要接收三个参数,完整的写法是:
![输入图片说明](https://gitee.com/uploads/images/2018/0604/160700_7fac1247_1844628.png "屏幕截图.png")

  - ClassLoader loader:指定当前目标对象使用类加载器,获取加载器的方法是固定的
  - Class<?>[] interfaces:目标对象实现的接口的类型,使用泛型方式确认类型
  - InvocationHandler h:事件处理,执行目标对象的方法时,会触发事件处理器的方法,会把当前执行目标对象的方法作为参数传入

- 代理类的实现
  - 代理类需要实现接口 **InvocationHandler** 
  - 代理类myInvocationHandler有实例域为Object的目标对象，构造函数包含目标对象；
  - invoke方法是执行代理时会执行的方法，所以我们所有代理需要执行的逻辑都会写在这里面
      - invoke(Object obj,Method method, Object[] args)。在实际使用时，第一个参数obj一般是指代理 类，method是被代理的方法，如上例中的getName()，args为该方法的参数数组。 这个抽 象方法在代理类中动态实现。
  - invoke方法里面的method可以使用java 反射调用真实的实现类的方法，我们在这个方法周围做一些代理逻辑工作就可以了
  - method.invoke(target, args); 参数为目标对象与目标参数

- 实现代码

![输入图片说明](https://gitee.com/uploads/images/2018/0604/155801_8b0c2e03_1844628.png "屏幕截图.png")

- 输出结果

![输入图片说明](https://gitee.com/uploads/images/2018/0604/155855_a31a79ee_1844628.png "屏幕截图.png")


**总结： 动态代理不需要实现接口，但是目标对象必须实现接口，否则不能用动态代理**


# 三、cglib代理
- Cglib代理,也叫作子类代理,它是在内存中构建一个子类对象从而实现对目标对象功能的扩展.
- Cglib是一个强大的高性能的代码生成包,它可以在运行期扩展java类与实现java接口.它广泛的被许多AOP的框架使用,例如Spring AOP和synaop,为他们提供方法的interception(拦截)
- Cglib包的底层是通过使用一个小而快的字节码处理框架ASM来转换字节码并生成新的类.不鼓励直接使用ASM,因为它要求你必须对JVM内部结构包括class文件的格式和指令集都很熟悉.
- 代理的类不能为final,否则报错
- 目标对象的方法如果为final/static,那么就不会被拦截,即不会执行目标对象额外的业务方法.


## CGLIB的核心类：
- net.sf.cglib.proxy.Enhancer – 主要的增强类
- net.sf.cglib.proxy.MethodInterceptor – 主要的方法拦截类，它是Callback接口的子接口，需要用户实现
- net.sf.cglib.proxy.MethodProxy – JDK的java.lang.reflect.Method类的代理类，可以方便的实现对源对象方法的调用,如使用：Object o = methodProxy.invokeSuper(proxy, args);//虽然第一个参数是被代理对象，也不会出现死循环的问题。
---

- net.sf.cglib.proxy.MethodInterceptor接口是最通用的回调（callback）类型，它经常被基于代理的AOP用来实现拦截（intercept）方法的调用。这个接口只定义了一个方法
- public Object intercept(Object object, java.lang.reflect.Method method,Object[] args, MethodProxy methodProxy) throws Throwable;
  - object是代理对象
  - Method 拦截的方法 （目标方法）
  - args 拦截的方法的参数 （目标方法的参数）
  - methodProxy JDK的java.lang.reflect.Method类的代理类，可以方便的实现对源对象方法的调用
原来的方法可能通过使用java.lang.reflect.Method对象的一般反射调用，或者使用 net.sf.cglib.proxy.MethodProxy对象调用。 **net.sf.cglib.proxy.MethodProxy通常被首选使用，因为它更快。** 

- 方法一

![输入图片说明](https://gitee.com/uploads/images/2018/0604/162430_8257b524_1844628.png "屏幕截图.png")

- 方法二

![输入图片说明](https://gitee.com/uploads/images/2018/0604/170320_29ab3df2_1844628.png "屏幕截图.png")


方法一与方法二的区别详情见 https://blog.csdn.net/a837199685/article/details/68930987#t3
输出
![](https://gitee.com/uploads/images/2018/0604/165229_92b08898_1844628.png "屏幕截图.png")

 **打印出的方法名称为代理类的名称** 


---
参考：https://blog.csdn.net/qq30211478/article/details/76621296