# play-mvc   
>基于servlet的mvc框架   

## target
- 简单rest服务
- 基于mvc的web开发
- 使用方式与spring mvc相同

## overview
- 推荐使用纯注解、零配置的方式，目前实现的xml功能非常有限
- 视图模板仅支持jsp，不支持velocity、freemarker   
- 目前暂不支持：
    - 暂不支持@Autowired，需要显式配置Bean  
    - 暂不支持@Repository、@Resource  
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
  todo
```

start from [http://localhost:8999](http://localhost:8999)

## todo

- [ ] 支持 @PathVariable, @RequestParam 
- [ ] 使用内置tomcat直接启动mvc应用 

- [x] ...

## later

- [ ] 支持 @RequestHeader, @CookieValue, @SessionAttributes 
- [ ] 内置tomcat切换为外置 


## License

[MIT](http://opensource.org/licenses/MIT)




