# dev-log


- java.lang.NoSuchMethodError: javax.servlet.http.Part.getSubmittedFileName()Ljava/lang/String;  
tomcat7缺少方法  
          
- debug的demo调试用的是 webpack.config.demo.js， build用的是 webpack.config.js
- font awesome 字体图标在firefox浏览器无法正常显示，显示的是unicode编码  
本地开发时会出现这个问题，是由于firefox的安全策略导致的  
部署到生产环境的时候不会出现这个问题    
