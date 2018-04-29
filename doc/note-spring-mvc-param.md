# spring mvc 参数解析

- 参数解析类

- RequestParamMethodArgumentResolver   
处理类型： 
    1、包含注解RequestParam，但不处理参数类型为Map，且不包含value值 
    2、参数类型为MultipartFile，或javax.servlet.http.Part的类型 
处理方式： 
    1、如果参数类型为MultipartFile，返回MultipartFile 
    2、如果参数类型为List<MultipartFile>，返回List<MultipartFile> 
    3、如果参数类型为javax.servlet.http.Part，返回Part 
    4、否则返回request.getParameter("key") 
    
- RequestParamMapMethodArgumentResolver   
处理类型： 
    1、参数包含RequestParam注解，且注解的value值为空，且参数类型为Map 
处理方式： 
    1、把parameter以map形式保存，如果value有多个值，只取第一个 
    
- PathVariableMethodArgumentResolver 
处理类型： 
    1、包含注解PathVariable，如果参数类型为Map，且PathVariable的value为空不处理 
处理方式： 
    1、根据把RequestMapping的url表达式，找到对应的值 
    2、把对应的值转换为参数类型对象 
    3、以View.class.getName() + ".pathVariables" 为key，参数值为value，放入request 的Attribute中 
    
- PathVariableMapMethodArgumentResolver   
处理类型：   
    1、包含注解PathVariable，且参数类型为Map，且PathVariable的value为空   
处理方式：   
    1、返回Map形式的对象 
    
- MatrixVariableMethodArgumentResolver 
处理类型： 
    1、包含注解MatrixVariable，如果参数类型为Map，且MatrixVariable的value为空不处理 
处理方式： 
    1、把对应的值反射到参数中 

- MatrixVariableMapMethodArgumentResolver 
  处理类型： 
      1、包含注解MatrixVariable，且参数类型为Map，且MatrixVariable的value为空 
  处理方式： 
      2、生产Map 
  ServletModelAttributeMethodProcessor 
  处理类型： 
      1、参数包含注解ModelAttribute 
  处理方式： 
      1、获取name，默认为value，如果没有则使用参数名 
      2、如果model包含了name的对象，这返回，如果没有则创建对象，然后把request.getParameter的值反射到对象中 
      3、将对象放入model中 
  RequestResponseBodyMethodProcessor 
  处理类型： 
      1、参数包含注解RequestBody 
  处理方式： 
      1、将post数据转换成对应的对象 
  RequestPartMethodArgumentResolver 
  处理类型： 
      1、包含注解RequestPart 
      2、类型为MultipartFile，但是没有注解RequestParam 
      3、类型为javax.servlet.http.Part 
  处理方式： 
      1、返回对象的类型数据 
  RequestHeaderMethodArgumentResolver 
  处理类型： 
      1、包含注解RequestHeader，且类型不是Map 
  处理方式： 
      1、返回request.getHeaderValues(name)[0] 
  RequestHeaderMapMethodArgumentResolver 
  处理类型： 
      1、包含注解RequestHeader，且类型是Map 
  处理方式： 
      1、把Heander转换成Map 
  ServletCookieValueMethodArgumentResolver 
  处理类型： 
      1、包含注解CookieValue 
  处理方式： 
      1、找到对应的cookie，如果参数类型为Cookie则返回cookie，如果是String，返回cookie的值 
  ExpressionValueMethodArgumentResolver 
  处理类型： 
      1、包含注解Value 
  处理方式： 
  ServletRequestMethodArgumentResolver 
  处理类型： 
  处理方式： 
  ServletResponseMethodArgumentResolver 
  处理类型： 
      1、处理类型为WebRequest.class.isAssignableFrom(paramType) || 
                  ServletRequest.class.isAssignableFrom(paramType) || 
                  MultipartRequest.class.isAssignableFrom(paramType) || 
                  HttpSession.class.isAssignableFrom(paramType) || 
                  Principal.class.isAssignableFrom(paramType) || 
                  Locale.class.equals(paramType) || 
                  InputStream.class.isAssignableFrom(paramType) || 
                  Reader.class.isAssignableFrom(paramType); 
  处理方式： 
      1、返回对应类型 
  RedirectAttributesMethodArgumentResolver 
  处理类型： 
      1、参数类型为RedirectAttributes 
  处理方式： 
  ModelMethodProcessor 
  处理类型： 
      1、处理类型为Model 
  处理方式： 
      1、返回Model 
  MapMethodProcessor 
  处理类型： 
      1、处理类型为Map 
  处理方式： 
      1、返回Model 
  ErrorsMethodArgumentResolver 
  处理类型： 
      1、处理类型为Errors 
  处理方式： 
      1、如果model没有数据，抛出IllegalStateException异常，否则返回BindingResult 
  SessionStatusMethodArgumentResolver 
  处理类型： 
      1、类型为SessionStatus 
  处理方式： 
      1、return mavContainer.getSessionStatus(); 
  UriComponentsBuilderMethodArgumentResolver 
  处理类型： 
      1、类型为UriComponentsBuilder 
  处理方式： 
      1、返回UriComponentsBuilder
