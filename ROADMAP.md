# roadmap for play-mvc

## todo  

- [ ] cglib实现aop 
- [ ] @Order 注解控制配置类的加载顺序
- [ ] aop织入顺序 @Order  注解支持
- [ ] getBean By class   
- [ ] 支持别名   
- [ ] xml新增支持constructor-args元素   
- [ ] 处理嵌套bean的问题   
- [ ] xml中同名bean抛出异常   
- [ ] 处理AOP中的循环依赖   

- [x] jdk动态代理实现aop 
- [x] 支持BeanPostProcessor   
- [x] 支持@ComponentScan配置basePackages
- [x] 手动通过注解注册bean生成BeanDefinition: @Configuration  @Bean   
- [x] 各种BeanDefinition转换成RootBeanDefinition   
- [x] ApplicationContext支持开启与关闭懒加载  
- [x] 扫描指定包带有@Component注解的bean   
- [x] xml bean元素支持id   
- [x] 属性默认为字符串，实现基本类型自动转换   
- [x] 支持ref为 object   
- [x] 解决二重循环依赖问题   
- [x] ApplicationContext默认立即初始化   
- [x] 抽象出 BeanDefinition 作为接口   
- [x] 抽象出 PropertyValues 作为接口   
- [x] 基本IoC

## later

- [ ] mybatis与spring纯注解整合   
- [ ] spring-jdbc原理  

- [ ] bean销毁的声明周期   
- [ ] BeanAware接口   
- [ ] BeanWrapper用于属性类型转换，暂未使用   
- [ ] 支持将bean的value类型配置为set,list   
- [ ] PropertyValues不支持合并   
- [ ] 通过可选懒加载更优雅地解决bean的循环依赖问题   
- [ ] MutablePropertyValues processedProperties  
- [ ] 在xml中使用anno：loadBeanDefinition时遇到component-scan元素时会以ComponentScanBeanDefinitionParser进行解析    
```
<context:annotation-config/>
<context:component-scan base-package="com.wtf.demo.spring.beans"/>
<bean id="ctxConfig" class="cn.javass.spring.chapter12.configuration.ApplicationContextConfig"/>
```

- [ ] 在anno中使用xml：使用注解@ImportResource
```xml
<bean id="message3" class="java.lang.String">
    <constructor-arg index="0" value="test"></constructor-arg>
</bean>
```
```java
@Configuration  
@ImportResource("classpath:/bookstore/config/spring-beans.xml")
  public class ApplicationContextConfig { }
```   
- [ ] 迁移到以注解为主的使用方式
- [ ] 没有处理桥接方法 findBridgedMethod()
