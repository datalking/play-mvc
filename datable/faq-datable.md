# faq-datable

## 约定

- [ ] 对在线excel的某些操作不会保存，未说明的操作都不会保存
     - Binding rows with headers 后移动行，不会保存修改
     - Hiding columns和Hiding rows不会保存
     
- [ ] 对在线excel的某些操作一定会保存
    - 数据修改编辑
    - moving rows和columns
    - 默认保存row和column的size

- [ ] 默认仅支持列级别的数据验证，不支持单元格级别的验证

- [ ] 默认支持 行或列 resize

- [ ] 默认支持auto fill，即拖拽填充

- [ ] 默认支持undo, redo

- 默认不支持 table in table
- 默认不支持 stretch
- 默认不支持 回到上次浏览的位置 / 打开文件时恢复上次浏览位置

- csv文件中引号的处理  
暂不支持批量取出双引号  

## faq

- Column summary 交互如何设计

- 表格中空值如何存储

- undo/redo支持那些操作

- 大量二维表数据存储需要解决的问题
    - 如何选择存储方式/格式
    - 如何保证查询效率
    - 如何保证更新/修改效率
        - 添加删除列
    - 如何保存原数据的顺序
    - 如何保存每个单元格的样式    
    
- 大量二维表数据的存储方案
    - rdbms
        - [x] 方案1: 用户上传一张excel就在数据库中新建一张表，缺点是用户量增长时小表太多；  
        - [ ] 方案2: 只考虑常用场景，预先创建固定列数的表，如预先创建列数从1-36的36张表，根据用户上传excel的列数存入对应的表，可以有效减少表数量； 
        - [ ] 方案3: 一个用户上传的所有Excel数据存入一张表，数据库表的一行分别为table_id、行号、列号、单元格值类型、具体值，此时上传的excel数据一个单元格对应数据库的一行，缺点是每次取一张表的数据查询的列太多； 
        - [ ] 方案4: 所有excel的数据存入一张表，与方案3类似。  
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
        - has_header
        
        - table_col_num
        - table_row_num
    
    - table_meta_header 支持nested_header
        - table_id
        - header_id
        - header_parent_id
        - header_level
        - header_label
        - header_tooltip
        - col_span
        - [也可以通过添加新列的方式来归一化嵌套列](https://stackoverflow.com/questions/40169711/mysql-db-table-structure-for-the-excel-table-having-multiple-sub-columns-under-s)　　
        
    - table_meta_column
        - table_id
        - column_index
        - column_name
        - column_type
    
    - table_meta_merge_cell
        - table_id
        - start_row_index
        - start_col_index
        - row_span
        - col_span
        
    - table_feature_info
        - search_enabled
        - sort_enabled
        - autocomplete_enabled
        - context_menu_enabled
    
    - table_cell_access
        - read_only
        - disallow_editing
    
    - table_cell_content
        - table_id
        - row_index
        - col_index
        - cell_content  
        
        - cell_comment
        - cell_style
        - validation
        
    ```
    - table_cell_style
        - row_index
        - col_index
        - style
    
    - table_cell_comment
    ```
    
    - table_source
        - table_id
        - from
    
    - table_tag
        - table_id
        - tag_name
    
    - user_permission

    - styles
        - html
        - align
        - format
        - border
        - font
        - date
        - stretch
        - highlight
        - password

 
        
- 备用
    - table_nested_row
        - table_id
        - row_index
        - child_table_id
        - child_table_row_index
    
    - table_formula
        - table_id
        - formula

   - table_fix
        - table_id
        - fixed_type: 1-row, 2-col
        - fixed_from_index
        - fixed_to_index
        
     - table_row_template
         - table_id
         - col_index
         - column_template_val
            
- 复合主键的查询, 想要索引命中, 必须得按照字段的顺序来查询, 否则就不会命中索引
- 联合主键比单主键维护开销更大，原因很简单，一棵更大的b树索引要维护
- 按照联合主键的建立顺序的查询，速度更好。原因也很简单，这个b树索引更细，能更快的检索到要查找的区间
- innodb 中的主键是聚簇索引，会把相邻主键的数据安放在相邻的物理存储上。
  如果主键不是自增，而是随机的，那么频繁的插入会使 innodb 频繁地移动磁盘块，而影响写入性能。
  
  

