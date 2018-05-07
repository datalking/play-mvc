# dev-log

- A child container failed during start LifecycleException: Failed to start component [StandardEngine[Tomcat].StandardHost[localhost
 将tomcat相关依赖改为provided

- mvc finishBeanFactoryInitialization(beanFactory); 

> 寻找方法时要寻找父类的方法，该使用getMethods()，而不是getDeclaredMethods() 
  
defaultServletHandlerMapping 找不到factoryMethodToUse    
mvcUrlPathHelper找不到factoryMethodToUse  
simpleControllerHandlerAdapter找不到factoryMethodToUse  
resourceHandlerMapping找不到factoryMethodToUse  
mvcContentNegotiationManager找不到factoryMethodToUse  
httpRequestHandlerAdapter找不到factoryMethodToUse  
mvcPathMatcher找不到factoryMethodToUse  
handlerExceptionResolver找不到factoryMethodToUse  
requestMappingHandlerMapping找不到factoryMethodToUse  
requestMappingHandlerAdapter找不到factoryMethodToUse  
viewControllerHandlerMapping找不到factoryMethodToUse    
