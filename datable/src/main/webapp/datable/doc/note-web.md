# note-web


## react

- 组件export时使用装饰器会改变ref
- React 的 props 有两个是私有的：key 和 ref，这两者是不能作为普通props传递给子组件的。
- ajax
    ```
    There is a common misconception that fetching in componentWillMount lets you avoid the first empty rendering state.
    In practice this was never true because React has always executed render immediately after componentWillMount. 
    If the data is not available by the time componentWillMount fires, the first render will still show a loading state regardless of where you initiate the fetch.
     This is why moving the fetch to componentDidMount has no perceptible effect in the vast majority of cases.
    ```

### features

- 17.0
    - deprecated: componentWillMount()，componentWillUpdate()，componentWillReceiveProps()
        - 关于提早发送数据请求，官方鼓励将数据请求部分的代码放在组件的constructor()中，将现有componentWillMount中的代码迁移至componentDidMount即可
        - new: getSnapshotBeforeUpdate()与componentDidUpdate()一起使用可以取代componentWillUpdate
        - new: getDerivedStateFromProps()与componentDidUpdate()一起使用可以取代componentWillReceiveProps

- 16.3
    - new Context API：父组件向嵌套内层子组件传递props
    - createRef：在编码中提前声明需要获取 Ref
    - ForwardRef：用于高阶组件传递ref，使包裹的无状态组件可以接收ref作为第二个参数，并且可以传递下去。
    - StrictMode：用于在开发环境下提醒组件内使用不推荐写法和即将废弃的API，不会被渲染成真实DOM

- 16.0
    - React Fiber：使得大量的计算可以被拆解分片，异步化
    - ReactDOM.createPortal：解决modal不需要渲染到parent node的问题
    - Fragment可以让聚合一个子元素列表，并且不在DOM中增加额外节点。
    - render()方法支持返回数组或返回单个字符串
    - ErrorBoundary：componentDidCatch()捕获render()时的错误
    - react dom/propTypes分离
    - License to MIT 

## DOM

- <dl><dt><dd> 定义带缩进的列表

## window
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


## misc

- 渐进式图片  
JPEG、GIF和PNG这三种图像格式都提供了一种功能，让图像能够更快地显示。图像可以以一种特殊方式存储，显示时先大概显示图像的草图，当文件全部下载后再填充细