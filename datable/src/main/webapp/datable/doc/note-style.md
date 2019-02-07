# note style

## dev tip
- 行内元素同样具有盒子模型
    - 行内元素的padding-top、padding-bottom、margin-top、margin-bottom属性设置是无效的
    - 行内元素的padding-left、padding-right、margin-left、margin-bottom属性设置是有效的
- z-index
    - For a positioned box (that is, one with any position other than static), Overlapping elements with a larger z-index cover those with a smaller one.
    - default value is `auto` 
- scss中的class不要通过`import styles from './index.scss'`这样的方式引入，`styles.className`未生效
- css direction: rtl, ltr - 指的是从left到right或相反
- 项目配色板 color palette
    - https://www.canva.com/learn/brand-color-palette
- `display: contents`能使拥有该属性的元素本身不能生成任何盒模型，但是它的子元素或者伪元素可以正常生成，该元素就好像在Dom树中被子元素与伪元素所替代一样
    - 使div元素不产生任何边框，因此元素的背景、边框和填充部分都不会渲染
    - 但该元素上的其他样式还是能够影响子元素
    - 该元素就会像是不存在一样，它的子元素会替代它在Dom树中的位置
    - 如果想添加一些有语义但又不显示的元素，那么该属性是非常有用的
- 媒体查询是向不同设备提供不同样式的一种方式
    - 媒体类型Media Type是一个常见的属性，在css2中支持的设备类型共10种，常用screen、print、all、tv、tty
    - 媒体特性Media Query是css3对Media Type的增强版、其实可以将Media Query看成Media Type(判断条件) + css (符合条件的样式规则)
    - 媒体特性有时候不止一条，当出现多个条件，就需要通过关键词连接，如and, not, only
    - 断点，就是设备宽度的临界点。在Media Query中，媒体特性 min-width 和 max-width 对应的属性值就是响应式设计中的断点值
    - 参考bulma的breakpoints： https://bulma.io/documentation/overview/responsiveness/
    - 参考媒体查询介绍 http://www.xsuchen.com/detail/css/11 http://www.xsuchen.com/detail/css/9.html
- pseudo elements: 4
    - before, after, first-letter, first-line
    - selector.class:pseudo-element {property:value;}
- pseudo classes: 7
    - a:link, a:visited, a:hover, a:active
    - :focus, :lang
    - first-child
- `.styl`样式文件是stylus扩展css语法的一种格式，stylus is similar to sass/less, except that it's based on nodejs.
    
## css style guide  
- https://github.com/airbnb/css

## bulma docs
- By default, columns are only activated from tablet onwards. This means columns are stacked on top of each other on mobile.
- Bulma is compatible with all icon font libraries: Font Awesome 5, Font Awesome 4, Material Design Icons, Open Iconic, Ionicons etc.  

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

## font icons

- catalog
    - https://getbootstrap.com/docs/4.0/extend/icons/
    - http://iconfont.cn/
- free
    - Fork Awesome
        - A fork of the Font-Awesome 4.7
        - https://forkawesome.github.io/Fork-Awesome/
            - https://github.com/ForkAwesome/Fork-Awesome
        - MIT
        - 733 icons
    - feather icons
        - beautiful open source icons. Each icon is designed on a 24x24 grid with an emphasis on simplicity, consistency and readability.
        - https://feathericons.com
            - https://github.com/feathericons/feather
            - https://github.com/carmelopullara/react-feather
        - MIT
        - 271 icons
    - font awesome 4.7
        - gives you scalable vector icons that can instantly be customized — size, color, drop shadow, and anything that can be done with the power of CSS.
        - https://fontawesome.com/v4.7.0/
            - https://github.com/FortAwesome/Font-Awesome/tree/v4.7.0
            - https://github.com/AndreLZGava/font-awesome-extension
        - CC BY 4.0
        - 675 icons
    - ionicons
        - open-source icon set with 700+ icons crafted for web, iOS, Android, and desktop apps. Ionicons was built for Ionic Framework
        - https://ionicons.com/
            - https://github.com/ionic-team/ionicons
        - MIT
        - 700+ icons
        - 图标数量较少
    - icono
        - One tag One icon, no font or svg, Pure CSS
        - https://saeedalipoor.github.io/icono/
        - https://github.com/saeedalipoor/icono
        - MIT
        - 130+ icons
        - 纯css实现的icon set，自己制作和添加图标不方便，css在不同浏览器渲染效果可能不同
            - https://github.com/wentin/cssicon
                - Creative Commons Zero v1.0 Universal
                - 512+ icons
            - 
    - line awesome
        - Replace Font Awesome with modern line icons
        - https://icons8.com/line-awesome
        - https://github.com/icons8/line-awesome
        - MIT/GBL GOOD BOY LICENSE
        - 674 icons
        - more icons
            - https://github.com/icons8/flat-color-icons
            - https://github.com/icons8/webicon
            - https://icons8.com/
            - animated: https://github.com/icons8/titanic
    - jam icons
        - icons shipped in JavaScript, font & SVG versions
        - https://jam-icons.com/
        - https://github.com/michaelampr/jam
        - MIT
        - 896 icons
    - open-iconic
        - the open source sibling of Iconic. It is a hyper-legible collection of 223 icons with a tiny footprint—ready to use with Bootstrap and Foundation
        - https://useiconic.com/open/
            - https://github.com/iconic/open-iconic
        - MIT
        - 223 icons 
        - dead project
    - google material design icons
        - Each symbol is available in five themes and a range of downloadable sizes and densities.
        - https://material.io/tools/icons/?style=outline
            - https://github.com/google/material-design-icons
        - Apache 2.0
        - 1k+ icons
    - bytesize icons
        - Tiny style-controlled SVG iconset
        - https://danklammer.com/bytesize-icons/
            - https://github.com/danklammer/bytesize-icons
        - MIT
        - 94 icons, 10kb
    - octicons
        - Octicons are a set of SVG icons built by GitHub for GitHub.
        - https://octicons.github.com/
          - https://github.com/primer/octicons/
        - MIT
        - 150+ icons
    - glyph
        - a semantic and versatile SVG icon set designed for customization.Glyph is 16x16, but because it's SVG, it can be any size you want.
        - http://glyph.smarticons.co/
            - https://github.com/frexy/glyph-iconset/
        - Attribution-ShareAlike 4.0 International (CC BY-SA 4.0) 署名-相同方式共享
        - 800+ icons
    - IKONS
        - Hand crafted, scalable vector icons，with 300 custom icons in SVG, AI, ESP, PSD, CSH and PNG format.
        - http://ikons.piotrkwiatkowski.co.uk/index.html
        - free to use these icons for personal and commercial work without obligation of payment or attribution.You may not redistribute or sell these icons
        - 300+ icons
    - dripicons
        - free vector line iconset by Amit Jakhu.
        - http://demo.amitjakhu.com/gg/
            - https://github.com/amitjakhu/dripicons
        - Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)
        - 150+ icons
    - nerd fonts
        - Nerd Fonts is a project that patches developer targeted fonts with a high number of glyphs (icons). 
        - https://nerdfonts.com/
            - https://github.com/ryanoasis/nerd-fonts#tldr
        - MIT
    -  Seti-UI
- paid
    - font awesome 5
        - Version 5 has been re-written and re-designed completely from scratch. 
        - https://fontawesome.com/how-to-use/on-the-web/setup/upgrading-from-version-4
        - https://github.com/FortAwesome/Font-Awesome
        - CC BY 4.0.  
            - free: solid, brands
            - paid: regular, light
        - 414 brand icons: weixin, qq, alipay, zhihu
- icons for industry
    - https://konpa.github.io/devicon/
        - icons representing programming languages, designing & development tools
        - v2: https://github.com/konpa/devicon
        - v1: https://github.com/vorillaz/devicons/
        - 78 icons and 200+ versions
    - https://github.com/simple-icons/simple-icons
        - SVG icons for popular brands
        - Creative Commons Zero v1.0 Universal 无著作权，可商用
        - 500+ icons
    - http://labs.mapbox.com/maki-icons/
        - poi icon set made for cartographers
        - https://github.com/mapbox/maki
    - https://erikflowers.github.io/weather-icons/
        - weather themed icons
        - https://github.com/erikflowers/weather-icons
- other font icons
    - https://github.com/snwh/paper-icon-theme
    - https://github.com/stark/siji
    - https://materialdesignicons.com/
    - font-family: Wingdings
- tools for icons
    - https://icomoon.io
    - http://fontastic.me
    - Typefont
        - The first open-source library that detects the font of a text in a image.
        - https://github.com/Vasile-Peste/Typefont
- tips
    - Moving Away From Font Icons
        - https://blog.ionicframework.com/announcing-ionicons-v4/
        - When we originally released Ionicons, we went the route of making an icon font. At the time, this made a lot of sense: 
            - Icon fonts are vector-based, 
            - scalable to any physical size without pixelation, 
            - able to be styled with CSS,  
            - come from a single resource (which means fewer HTTP requests).
        - they also have a cost that can flip their advantage into a disadvantage: all of the icons are bundled in one file. And in previous versions of Ionicons, we were packing nearly 700 icons into one large file!
            - Large font files have a negative impact on a webpage or app’s Time to First Paint, which makes for a subpar user experience and has been known to lower PWA Lighthouse scores. 
            - On top of that, adding any custom icons to a font icon is far from easy.
        - All of the benefits of font icons (vector-based and styling with CSS) can be achieved just as easily with SVGs, but without all the baggage.

## fonts

- free
    - catalog
        - http://zenozeng.github.io/Free-Chinese-Fonts/
        - https://www.zhihu.com/question/19727859
    - Source Han Sans
        - https://fonts.adobe.com/fonts/source-han-sans-simplified-chinese
            - https://github.com/adobe-fonts/source-han-sans
        - SIL Open Font License
        - the Source Han Sans and Noto Sans CJK typeface families are mechanically identical
            - https://github.com/adobe-fonts/source-han-sans/issues/122
    - Adobe's open source family
        - source code pro
        - source sans pro
        - source serif pro
        - source han sans/noto sans cjk/思源黑体
        - source han serif/noto serif cjk/思源宋体
        - 思源柔黑，由于这款字体是日文改造，所以其中大多都是繁体字，如果要使用这款字体，建议切换繁体输入法
    - 方正免费字体
        - 方正书宋、方正仿宋、方正黑体、方正楷体
        - 免费商用
            - https://www.foundertype.com/index.php/About/powerbus.html
    - 站酷免费字体7种
        - 站酷高端黑体、站酷酷黑体、站酷快乐体、站酷庆科黄油体、站酷文艺体、站酷小薇LOGO体
        - 免费商用
            - https://www.zcool.com.cn/special/zcoolfonts/
    - 王汉宗自由字形 (H.T.Wang Free Fonts)
        - https://github.com/cghio/wangfonts
        - GPL v2
    - 文泉驿字体
        - 文泉驿微米黑、文泉驿正黑
        - http://wenq.org/wqy2/index.cgi
        - GPL
    - 文鼎公众授权字体
        - 文鼎细上海宋、文鼎中楷、文鼎简报宋、文鼎简中楷、文鼎PL明体U20-L、文鼎PL报宋2GBK
        - http://www.arphic.com.tw/
        - 非商业免费用
    - 新蒂字体
        - 新蒂文徵明体免费版、新蒂小丸子小学生版、新蒂小丸子高级版、新蒂下午茶基本版
        - https://www.sentyfont.com/index.htm
        - 非商业免费用
    - 造字工房字体
        - 非商业免费用
    - Droid Sans Fallback
        - apache 2.0
        - Android设备初期时默认的中文字体，由谷歌委托台湾华康科技设计的，与微软雅黑很像
    - fandol fonts
        - GPL
        - https://github.com/guoyu07/fandol-fonts
    - 851 Chikara Dzuyoku is a hand-drawn Japanese font 
        - Free for personal, non-commercial, and commercial works
- western fonts
    - catalog   
        - https://www.fontsquirrel.com/
    - Roboto
        - https://github.com/google/roboto/
        - Apache 2.0
    - ZCOOL Addict Italic 站酷意大利体
    - Arual
        - 免费商用
        - https://www.dafont.com/arual.font
        - http://www.fonts.net.cn/font-18934379648.html
        - 无衬线
- other fonts
    - Airbnb Cereal
        - A new typeface that takes us from button to billboard.
        - https://airbnb.design/cereal/
    - 小米兰亭
        - http://www.miui.com/zt/miui8/index.html
    - 书体坊字体付费标准
        - http://blog.sina.com.cn/s/blog_4e6ac4af0102wpqg.html
    - 濑户字体
    - 华康字体，免费仅限用于阿里巴巴旗下网站
- tools for fonts
    - https://github.com/wentin/font-playground
        - 在线修改和预览
## animation

- https://github.com/ConnorAtherton/loaders.css