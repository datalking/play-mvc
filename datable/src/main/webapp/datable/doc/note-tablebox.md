# note-tablebox
simple table data etl tool in java

## overview
- 基于poi读写excel的工具
- 读写csv

### NOT
- 不会实现操作数据库
- 不会实现统计分析

### 需求分析
- 读写excel

- 从网页上传文件或本地文件读取excel文件转换成对象

### 目标明确
- excel的etl


### 功能

- excel文件格式相互转换：03 <-> 07

- 表格数据导出03/07格式的excel文件
- 表格数据导出csv
- 表格数据导出html
- 基于模板导出excel



- 读取03/07格式的excel文件
- csv转换成excel
- html转换成excel，即将html的table解析成excel
    - 维基百科数据特殊支持

- 导入导出时，忽略或添加表头
- 支持生成的excel包含多张sheet
- 导入导出时指定所需的列
- 修改列的顺序
- excel的列的顺序默认为bean中成员变量的顺序

- 导出时支持根据预先配置的模板样式


### faq

- 表格数据在内存中的形式转换     
    - List<Map>
    - List<Bean>
        - https://github.com/xuxueli/xxl-excel
        - https://github.com/Crab2died/Excel4J
        - https://github.com/liaochong/myexcel/wiki
        - 缺点：导入时需要指定Bean对应的class
    - List<List<String>> 
        - https://github.com/Crab2died/Excel4J
        - https://github.com/amlongjie/ExcelParser 


- 空白格的处理策略

### 全局约定及默认值

- 创建Shhet时默认名称是Sheet1, Sheet2, ..., SheetN



