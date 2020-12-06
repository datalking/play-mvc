# play-mvc   
>基于java servlet开发的mvc框架   

## target
- 精简spring的源码，使用方式与spring mvc相同
- 基于mvc快速开发rest服务
- 基于mvc快速开发webapp

## overview
- 精简了spring的源码[spring-framework](https://github.com/spring-projects/spring-framework)和[spring-boot](https://github.com/spring-projects/spring-boot) 
- 推荐使用纯注解、零配置文件的方式，目前实现的xml功能非常有限
- 视图模板仅支持jsp，无计划支持jsf、velocity、freemarker等   
- 支持以下注解
    - 通用注解：@Component、@Configuration、@Bean、@EnableAspectJAutoProxy、@Aspect、@Before、@After
    - mvc相关：@Controller、@PathVariable、@RequestParam
- 目前暂不支持：
    - 暂不支持@Autowired，需要显式配置Bean  
    - 暂不支持@Repository、@Resource  
    - 暂不支持静态资源处理  
    - 不支持servlet2.5及以下的web容器，仅支持servlet3.0及以上的容器
    - 不支持introduction引入增强，仅支持weave  
    - 不支持指定aop生成代理对象的方式，默认使用JdkDynamicAopProxy，目标对象未实现接口时使用CglibAopProxy
    - 不支持动态代理指定构造函数参数
    - 不支持将bean的value类型配置为set,list,map，仅支持字符串和ref  
    - 不支持为bean指定别名
    - 不支持构造注入与方法注入，仅支持属性注入
    - ...
    

## dev 
```sh
 git clone https://github.com/datalking/play-mvc.git
 cd play-mvc/
 ./start-build-dev.sh
```

## demo
```sh
  cd play-mvc/starter-demo
  mvn tomcat7:run
```

start from [http://localhost:8999](http://localhost:8999)

## usage

## License
[MIT](http://opensource.org/licenses/MIT)




