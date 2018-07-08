# faq-datable

- csv文件中引号的处理  
暂不支持批量取出双引号  


- 大量二维表数据存储需要解决的问题
    - 如何选择存储方式/格式
    - 如何保证查询效率
    - 如何保证更新/修改效率
    - 如何保存原数据的顺序
    - 如何保存每个单元格的样式    
    
- 大量二维表数据的存储方案
    - rdbms
        - 在数据库中创建列数为1-24的N张表，数据库中每表的一行存储数据源excel的一行，缺点不利于单个用户的搜索
        - 彻底扁平归一化，一张大表存储数据源excel的行列号和该单元格的数据
    - nosql
        - mongodb:转换成json存储，缺点是不易修改和更新
        - hbase：表名作为rowkey的一部分
    - 文件 **不可取**
        - 本地文件
        - hdfs存储小文件的方法

- 数据库设计

    - user
        - user_id   
        - username
        - password
        
    - table_registry
        - table_id 
        - user_id
        - table_name
        
    - table_column
        - table_id
        - column_index
        - column_name
        - column_type
        
    - table1col
        - table_id
        - row_index
        - cell1
    -
    - 
    - table24col
    
    - table_cell_style
        - table_id
        - row_index
        - col_index
        - style
    
    - table_source
        - table_id
        - from
    
    - table_tag
        - table_id
        - tag_name
    
        
    
