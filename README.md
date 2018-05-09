# play-mvc   
>基于servlet的mvc框架   

## target
- 快速开发rest服务
- 基于mvc快速开发webapp
- 使用方式与spring mvc相同

## overview
- 推荐使用纯注解、零配置的方式，目前实现的xml功能非常有限
- 视图模板仅支持jsp，尚未计划支持jsf、velocity、freemarker等   
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
    
- 源码参考了[spring-framework](https://github.com/spring-projects/spring-framework)和[spring-boot](https://github.com/spring-projects/spring-boot) 

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

## todo

- [ ] 支持 @PathVariable
- [ ] 支持 @RequestParam 
- [ ] 支持 Redirect 
- [ ] 支持 文件上传 MultipartResolver 
- [ ] 使用内置tomcat直接启动mvc应用 
- [ ] servlet和filter支持 `async` 处理

- [x] 支持 @ResponseBody 
- [x] 扫描 DelegatingWebMvcConfiguration中的bean
- [x] add DispatcherServlet 

## later

- [ ] WebApplicationInitializer支持order 
- [ ] 支持 @RequestHeader, @CookieValue, @SessionAttributes 
- [ ] 内置tomcat切换为外置 

## License

[MIT](http://opensource.org/licenses/MIT)




