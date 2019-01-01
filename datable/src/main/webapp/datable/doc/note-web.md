# note-web


- html高度问题
    - window.innerWidth包括scrollBar
    - innerHeight是DOM视口的大小，包括内容、边框以及滚动条  
        - 会导致handsontable的滚动条显示不出来  
    - outerHeight是整个浏览器窗口的大小，包括窗口标题、工具栏、状态栏等。
    - document.documentElement.clientWidth不包括scrollBar

```
document.documentElement.clientHeight：不包括整个文档的滚动条，但包括<html>元素的边框
document.body.clientHeight：不包括整个文档的滚动条，也不包括<html>元素的边框，也不包括<body>的边框和滚动条
其中documentElement是文档根元素，就是<html>标签，body就是<body>标签了，这两种方式兼容性较好，可以一直兼容到IE6
```

- 滚动高度
    - clientHeight: 内部可视区域大小
    - offsetHeight：整个可视区域大小，包括border和scrollbar在内
    - scrollHeight：元素内容的高度，包括溢出部分
    - scrollTop：元素内容向上滚动了多少像素，如果没有滚动则为0   
    - `所有DOM元素都有上述4个属性，只需要给它固定大小并设置overflow:scroll即可表现出来`  

- dom位置
    - Event对象属性
          - clientX：相对于可视区域的x坐标。  
          - clientY：相对于可视区域的y坐标。   
          - screenX：相对于用户屏幕的x坐标。  
          - screenY：相对于用户屏幕的y坐标。  
          
    - IE特有属性
          - offsetX：鼠标相对于目标事件的父元素的内边界在x坐标的位置  
          - offsetY：鼠标相对于目标事件的父元素的内边界在y坐标的位置  
          - x：设置或获取鼠标指针位置相对于父文档的y坐标。同clientX  
          - y：设置或获取鼠标指针位置相对于父文档的y坐标。同clientY  
