# note style

## css style guide  
- https://github.com/airbnb/css

## css 模块化
CSS模块化方案，主要有三类。

- CSS命名约定  
规范化CSS的模块化解决方案（比如BEM,OOCSS,AMCSS,SMACSS,SUITCSS)
但存在以下问题：
    - JS CSS之间依然没有打通变量和选择器等
    - 复杂的命名

- CSS in JS  
彻底抛弃CSS，用JavaScript写CSS规则，并内联样式。 
React: CSS in JS // Speaker Deck。Radium，react-style 属于这一类。但存在以下问题：
    - 无法使用伪类，媒体查询等
    - 样式代码也会出现大量重复。
    - 不能利用成熟的 CSS 预处理器（或后处理器）

- 使用JS管理样式模块  
使用JS编译原生的CSS文件，使其具备模块化的能力，代表是CSS Modules（GitHub：css-modules/css-modules）  

### css naming conventions

- BEM
block__element--modifier


- SMACSS   
Scalable and Modular Architecture for CSS    
https://smacss.com/book/   

- OOCSS  
Object-Oriented CSS   
https://github.com/stubbornella/oocss/wiki  

- AMCSS 
属性模块或者说AM，其核心就是关于定义命名空间用来写样式。
通俗的讲就是，给一个元素加上属性，再通过属性选择器定位到这个元素，达到避免过多的使用class。  
