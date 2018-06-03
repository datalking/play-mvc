# note-java
java笔记

## faq

- java 8 接口 静态方法 原理

## java web

- servlet被加载的时机
    - load-on-startup元素用于配置web应用启动时servlet被加载的顺序，它的值必须是一个整数。
      如果它的值是一个负整数或是这个元素不存在，那么容器会在该servlet被调用的时候，加载这个servlet。
      如果值是正整数或零，容器在配置的时候就加载并初始化这个servlet，容器必须保证值小的先被加载。
      如果值相等，容器可以自动选择先加载谁。
    - init()方法是在Servlet实例化之后执行的，并且只执行一次
    - init(ServletConfig)中参数ServletConfig，代表的是配置信息，即在web.xml中配置的信息
    - init()方法是为了防止程序员在写Servlet类重写 init(ServletConfig config)时忘记写super.init(ServletConfig config)

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

- instanceof 在 java 的编译状态和运行状态是有区别的：
  - 在编译状态中 class 可以是 object 对象的父类、自身类、子类，在这三种情况下 java 编译时不会报错，
  - 在运行转态中 class 可以是 object 对象的父类、自身类，但不能是子类，当为父类、自身类的情况下 result 结果为 true，为子类的情况下为 false。
  
- Class.isInstance(obj) 表明这个对象能不能被转化为这个类，如果 obj 是调用这个方法的 Class 或接口的实例则返回true，这个方法是 instanceof 运算符的动态等价，如果 obj 为 null 则返回 false。
  
- java class name
    - 1、getCanonicalName顾名思义的正规的名字，与之对应的是getName
    - 2、大部分情况下，getName和getCanonicalName没有什么不同的， 但是对于array和内部类就不一样了
    - 3、对于数组：getCanonicalName是正规的（最后带有[]表示数组），getName是编译器的（前面带有[表示一维数组）
    - 4、对于内部类：getCanonicalName是空，getName是带有$的
    - 5、getSimpleName是简单的名字，是getName去掉了包名和$（内部类时候带有$）的余下的类自身的名字；getName带有包名和$（内部类时候带有$）
      
- java class
    - Class 类的对象表示 JVM 中的一个类或接口
    - 数组也被映射为Class对象，所有元素类型相同且维数相同的数组都共享同一个 Class 对象 。

- java.beans.PropertyEditor属性编辑器接口，它规定了将外部设置值转换为内部JavaBean属性值的转换接口方法
    - Object getValue()：返回属性的当前值。基本类型被封装成对应的包装类实例；
    - void setAsText(String text)：用一个字符串去更新属性的内部值，这个字符串一般从外部属性编辑器传入；
    - void setValue(Object newValue)：设置属性的值，基本类型以包装类传入（自动装箱），  
      setValue()一般不直接使用，在setAsText方法中将字符串进行转换并产生目标对象以后，由调setAsText调用setValue来把目标对象注入到编辑器中
    - String getAsText()：将属性对象用一个字符串表示，以便外部的属性编辑器能以可视化的方式显示。缺省返回null，表示该属性不能以字符串表示；
    - String[] getTags()：返回表示有效属性值的字符串数组（如boolean属性对应的有效Tag为true和false），以便属性编辑器能以下拉框的方式显示出来。缺省返回null，表示属性没有匹配的字符值有限集合；
    - String getJavaInitializationString()：为属性提供一个表示初始值的字符串，属性编辑器以此值作为属性的默认值。
- PropertyEditorSupport，该类实现了PropertyEditor接口并提供默认实现，一般情况下，用户可以通过扩展这个方便类设计自己的属性编辑器
- BeanInfo主要描述了JavaBean哪些属性可以编辑以及对应的属性编辑器，每一个属性对应一个属性描述器PropertyDescriptor
    - PropertyDescriptor的构造函数PropertyDescriptor(String propertyName, Class beanClass) ，其中propertyName为属性名，而beanClass为JavaBean对应的Class
    - PropertyDescriptor还有一个setPropertyEditorClass(Class propertyEditorClass)方法，为JavaBean属性指定编辑器
    - BeanInfo接口最重要的方法就是：PropertyDescriptor[] getPropertyDescriptors() ，该方法返回JavaBean的属性描述器数组

- Spring的属性编辑器和传统的用于IDE开发时的属性编辑器不同，它们没有UI界面，仅负责将配置文件中的文本配置值转换为Bean属性的对应值
- PropertyEditorRegistrySupport为常见属性类型提供了默认的属性编辑器，这些“常见的类型”可分为3大类
    - 基本类型
        - 1）基本数据类型，如：boolean、byte、short、int等；
        - 2）基本数据类型封装类，如：Long、Character、Integer等； 
        - 3）两个基本数据类型的数组，char[]和byte[]；
        - 4）大数类，BigDecimal和BigInteger
    - 集合类型
        - 为5种类型的集合类Collection、Set、SortedSet、List和SortedMap提供了编辑器
    - 资源类型
        - 用于访问外部资源的8个常见类Class、Class[]、File、InputStream、Locale、Properties、Resource[]和URL	
- defaultEditors：用于保存默认属性类型的编辑器，元素的键为属性类型，值为对应的属性编辑器实例；
- customEditors：用于保存用户自定义的属性编辑器，元素的键值和defaultEditors相同
- Spring大部分默认属性编辑器都直接扩展于java.beans.PropertyEditorSupport类，用户也可以通过扩展PropertyEditorSupport实现自己的属性编辑器。
  比起用于IDE环境的属性编辑器来说，Spring环境下使用的属性编辑器的功能非常单一：仅需要将配置文件中字面值转换为属性类型的对象即可，不需提供UI，因此仅需覆盖setAsText()方法
    
- Type 是Java类型体系中的顶级接口，Class是Type的一个直接实现类，Type有4个直接子接口：TypeVariable，WildcardType，ParameterizedType，GenericArrayType

- TypeVariable 表示类型变量，是各种类型变量的公共父接口。
  比如T，比如K extends Comparable<? super T> & Serializable，这个接口里面有个getBounds()方法，它用来获得类型变量上限的Type数组，如果没有定义上限，则默认设定上限为Object

- WildcardType 用来描述通配符表达式，如? super T，调用getUpperBounds()上限和getLowerBounds()下限这两个方法，获得类型变量?的限定类型(上下限)

- ParameterizedType 表示参数化类型，如java.lang.Comparable<? super T>，再比如List<T>，List<String>，这些都叫参数化类型。
  得到Comparable<? super T>之后，再调用 `getRawType()` 与 `getActualTypeArguments()` 两个方法，
  就可以得到声明此参数化类型的类(java.lang.Comparable)和实际的类型参数数组([? super T])，而这个? super T又是一个WildcardType类型。

- GenericArrayType 表示泛型数组，即一种元素类型是参数化类型或者类型变量的数组类型
```
// 以下属于 GenericArrayType
List<String>[] pTypeArray;
T[] vTypeArray;

// 以下不属于 GenericArrayType
List<String> list;
String[] strings;
Person[] ints;
```

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
    - getMethods()获取的是所有public方法，包括：类自身声明的public方法、父类中的public方法、实现的接口方法。不包括private和protected方法
    - getDeclaredMethods()获取的是本类中所有方法，包括：类自身的方法、重写的父类的方法、实现的接口方法。不包括继承自父类的方法。
      

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

