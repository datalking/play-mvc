# note-dev-js

## mdn docs

- `arr.slice()`   
	- arr.slice();  // [0, end]
	- arr.slice(begin);
	- arr.slice(begin, end)     
	- 方法返回一个新的数组对象，这一对象是一个由 begin和 end（不包括end）决定的原数组的浅拷贝。原始数组不会被改变。
- `array.splice(start[, deleteCount[, item1[, item2[, ...]]]])`    
	- start 指定修改的开始位置（从0计数）
	- deleteCount 整数，表示要移除的数组元素的个数
	- item1, item2, .. 要添加进数组的元素,从start 位置开始
	- 返回由被删除的元素组成的一个数组。如果只删除了一个元素，则返回只包含一个元素的数组。如果没有删除元素，则返回空数组。
	- splice()与slice()的作用是不同的，splice()会直接对数组进行修改
-  `arr.reverse()`  
	- reverse 方法颠倒数组中元素的位置，并返回该数组的引用
- `Object.assign(target, ...sources)`
	- method is used to copy the values of all enumerable own properties from one or more source objects to a target object. It will return the target object.
		- shallow copies property values. If the source value is a reference to an object, it only copies that reference value.

## nodejs  

- path
    - path.join():方法使用平台特定的分隔符把全部给定的 path 片段连接到一起，并规范化生成的路
    - path.resolve:方法会把一个路径或路径片段的序列解析为一个绝对路径。 
    
## dev tips

- js equals
    - `==`：等同，比较运算符，两边值类型不同的时候，先进行类型转换，再比较；
    - `===`：恒等，严格比较运算符，不做类型转换，类型不同就是不等；
    - `Object.is()`是ES6新增的用来比较两个值是否严格相等的方法，与===的行为基本一致。
- window.location.assign(url) ： 加载 URL 指定的新的 HTML 文档，支持返回上个页面
- window.location.replace(url) ： 通过加载 URL 指定的文档来替换当前文档 ，不支持返回
- Object.assign()与spread operator扩展运算符 区别
	- 扩展运算符支持遍历数组
	- 扩展运算符 is A proposal, not standardized, Literal, not dynamic.
		- 动态示例 `options = Object.assign.apply(Object, [{}].concat(sources))`

- HTTP cookies
	- An HTTP cookie (web cookie, browser cookie) is a small piece of data that a server sends to the user's web browser. 
	- The browser may store it and send it back with the next request to the same server. 
	- Typically, it's used to tell if two requests came from the same browser — keeping a user logged-in, for example. 
	- It remembers stateful information for the stateless HTTP protocol.
	- Cookies are mainly used for three purposes:
		- Session management
			- Logins, shopping carts, game scores, or anything else the server should remember
		- Personalization
			- User preferences, themes, and other settings
		- Tracking
			- Recording and analyzing user behavior
	- 浏览器在发送请求之前，首先会根据请求url中的域名在cookie列表中找所有与当前域名一样的cookie，然后再根据指定的路径进行匹配，
	   如果当前请求在域匹配的基础上还与路径匹配那么就会将所有匹配的cookie发送给服务器，这里要注意的是最大匹配和最小匹配问题，
	   有些cookie服务器在发送之前会有意扩大当前页面cookie的匹配范围，此时这些被扩大范围的cookie也会一起发送给服务器。

- XMLHttpRequest与fetch   
	- XMLHttpRequest是对象，fetch()是window的一个方法
	- 服务器返回400、500错误码时并不会 reject，只有网络错误这些导致请求不能完成时，fetch 才会被 reject 
	- 在默认情况下fetch不会接受或者发送cookies，需要设置 `fetch(url, {credentials: 'include'})`
	- 所有的IE浏览器都不会支持 fetch()方法
	- fetch不支持JSONP
	- fetch不支持progress事件
	- fetch不支持超时timeout处理

- 函数去抖（debounce）与 函数节流（throttle）
	-  以下场景往往由于事件频繁被触发，因而频繁执行DOM操作、资源加载等重行为，导致UI停顿甚至浏览器崩溃。
		- window对象的resize、scroll事件
		- 拖拽时的mousemove事件
		- 射击游戏中的mousedown、keydown事件
		- 文字输入、自动完成的keyup事件
		- 实际上对于window的resize事件，实际需求大多为停止改变大小n毫秒后执行后续处理；而其他事件大多的需求是以一定的频率执行后续处理。针对这两种需求就出现了debounce和throttle两种解决办法。
	- js debounce 去抖
	
		- 当调用动作n毫秒后，才会执行该动作，若在这n毫秒内又调用此动作则将重新计算执行时间。
		- 示例
		```
		var debounce = function(idle, action){
		  var last
		  return function(){
			var ctx = this, args = arguments
			clearTimeout(last)
			last = setTimeout(function(){
				action.apply(ctx, args)
			}, idle)
		  }
		}
		```  
	- js throttle 节流   
		- 预先设定一个执行周期，当调用动作的时刻大于等于执行周期则执行该动作，然后进入下一个新周期
		- 示例
		```
		var throttle = function(delay, action){
		  var last = 0return function(){
			var curr = +new Date()
			if (curr - last > delay){
			  action.apply(this, arguments)
			  last = curr 
			}
		  }
		}
		```
	- 函数节流和函数去抖都是通过减少实际逻辑处理过程的执行来提高事件处理函数运行性能的手段，并没有实质上减少事件的触发次数

- npm    
	- npx makes it easy to use CLI tools and other executables hosted on the registry.