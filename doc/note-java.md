# note-java
java笔记

## faq

- java 8 接口 静态方法 原理

## java web

- request.getAttribute() 和 request.getParameter() 区别
    - attribute 值来源于 【Web 服务器端】，而parameter 值来源于【浏览器端】
        - request.getAttribute() 的值来源于【 request.setAttribute() 】方法设置的值。
        - equest.getParameter() 的值来源于 页面通过【get或者post】方式传上来的参数值没有setParameter()
    - attribute 值类型是【Object】类型，而parameter 值类型为【String】类型

- 部署之后修改jsp文件仍然会改变网页内容

- RequestDispatcher是一个Web资源的包装器,可以用来把当前request传递到该资源,或者把新的资源包括到当前响应中

- Servlet 3.0标准中含有一个 ServletContainerInitializer 接口，所有实现了这个接口的类会在容器启动的时候得到一个通知，并且会调用其 onStartup()方法
- SpringServletContainerInitializer类，实现了ServletContainerInitializer
- 当容器启动时，会到应用程序中搜索所有实现或继承了 WebApplicationInitializer类型的类，并且将这些类作为参数传递给 SpringServletContainerInitializer.onStratup()方法

## summary


- jdk动态代理生成执行before、after的打印结果可以通过System.setOut重定向来获取

- java反射创建对象
    - Class.newInstance() 只能够调用无参的构造函数，即默认的构造函数； 
    - Constructor.newInstance() 可以根据传入的参数，调用任意构造构造函数。 
    - Class.newInstance() 要求被调用的构造函数是可见的，也即必须是public类型的; 
    - Constructor.newInstance() 在特定的情况下，可以调用私有的构造函数。 

- 当参数是可变长的rest参数时，取到参数变量当做数组处理

- A.isAssignableFrom(B)
    - 判断 A是否是B的父类或父接口，即 B extends A
    - A与B必须同为类或同为接口

- java 注解
    - AnnotatedElement接口代表程序中可以接受注解的程序元素.像Class Constructor FieldMethod Package这些类都实现了AnnotatedElement接口.

- java 类型与实例判断
    - instanceof
        - 自身实例或子类实例 instanceof 自身类   返回true
    - isInstance
        - 自身类.class.isInstance(自身实例或子类实例)  返回true
    - isAssignableFrom
        - 自身类.class.isAssignableFrom(自身类或子类.class)  返回true
        
- AspectJ可以作为静态aop，在编译阶段完成对程序的修改

- java 反射
    - getDeclaredField()：是可以获取一个类的所有字段  
    - getField()：只能获取类的 `public` 字段 
    - getDeclaredAnnotations()：获取元素上的所有注解，该方法将忽略继承的注解，如果没有注释直接存在于此元素上，则返回长度为零的一个数组
    - getAnnotations()：返回该程序元素上存在的所有注解
    - Method.isBridge()：判断是否是桥接方法，对于覆盖父类或接口方法时有用
- java 字符串 替换
    - replace函数只实现简单的替换功能，默认替换所有
    - replaceAll函数实现了正则表达式替换功能。
- java 安全管理器
    - AccessController.doPrivileged意思是这个是特别的,不用做权限检查，使用场景：
        - 假如1.jar中有类可以读取一个文件，但是我们的类本生是没有权限去读取那个文件的，在1.jar中如果读取文件的方法是通过doPrivileged来实现的.就不会有后面的检查了
- java 类加载
    - java中class.forName()和classLoader都可用来对类进行加载
    - class.forName()前者除了将类的.class文件加载到jvm中之外，还会对类进行解释，执行类中的static块。
    - classLoader只干一件事情，就是将.class文件加载到jvm中，不会执行static中的内容,只有在newInstance才会去执行static块。
    
- 方法的参数为lambda时，会先进入方法内，再计算lambda参数

- java读取xml `DocumentBuilderFactory.newInstance().newDocumentBuilder().parse('file.xml')`
    - Node接口是代表了文档树中的抽象节点，多使用Node子对象Element,Attr,Text
    - xml元素类型判断
    ```xml
    <beans top="beansAttr">
        <bean name="beanAllStr" class="com.github.datalking.bean.BeanAllStr">
            <property name="id" value="helloId"/>
        </bean>
        <bean name="dataAnalyst" class="com.github.datalking.bean.DataAnalyst">
            <property name="name" value="helloName"/>
        </bean>
    </beans>
    ```  
    根节点有5个childNode，3个Text，2个Element

