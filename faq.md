# faq for play-mvc

- intellij idea的bug？   
只有被依赖的项目在下面，当前项目才能运行？
demo-quickstart只有放到上面才能运行?

- play-mvc没有使用父子容器的设计，源码上支持spring的父子容器，但推荐将parent即RootApplicationContext设为null

- play-ioc支持的spring常用配置标签，推荐使用注解
    - [x] property   
    - [ ] constructor
   
- BeanDefinition
    - GenericBeanDefinition暂不支持父子结构，无计划实现
    - RootBeanDefinition定义表明它是一个可合并的bean definition，不会单独实现，使用GenericBeanDefinition也可实现  

- 配置的属性值注入bean时，为什么要经过2次类型转换？(在applyPropertyValues()方法中)  
    - `Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);`
    - `convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);`
    - 两种方式最后都是调用 `TypeConverterDelegate.convertIfNecessary()`  
    
- BeanFactory默认懒加载，只有调用getBean()时才创建实例 

- ApplicationContext默认立即加载，通过主动调用getBean()来实现，只有ApplicationContext才能选择是否开启懒加载

- 同时使用xml和注解注入bean的属性，谁的优先级更高？  
    - 当出现两个相同名称实例，spring会覆盖其中一个，xml优先级高于注解  
    - xml中同时配置两个相同id的bean，直接校验不通过报错
    - 多次注入同一个bean的，如果beanName不一样的话，那么会产生两个Bean；如果beanName一样的话，后面注入的会覆盖前面的





