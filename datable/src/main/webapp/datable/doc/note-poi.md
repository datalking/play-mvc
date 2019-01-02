# note poi 


- 操作excel图表的3种思路
    - 定义包含图表的excel模板，通过改变数据来改变图表
    - jfreechart + poi ：先生成图表图片，再插入excel，每次修改图表都要重新生成图片插入excel
    - java调用vbs，vbs调用excel的宏。

## Apache POI - HSSF and XSSF Limitations  
 known limitations of the POI HSSF and XSSF APIs:  
 
 - file size & memory usage      
There are some inherent limits in the Excel file formats. These are defined in class SpreadsheetVersion.   
As long as you have enough main-memory, you should be able to handle files up to these limits.  
There are ways to overcome the main-memory limitations if needed:   
    - For writing very huge files, there is SXSSFWorkbook which allows to do a streaming write of data out to files.
    - For reading very huge files, take a look at the sample XLSX2CSV which shows how you can read a file in streaming fashion.

- charts   
    - HSSF has some limited support for creating a handful of very simple Chart types, but largely this isn't supported. HSSF (largely) doesn't support changing Charts.
    - XSSF has only limited chart support including making some simple changes and adding at least some line and scatter charts
    
- Macros  
Macros can not be created. The are currently no plans to support macros. However, reading and re-writing files containing macros will safely preserve the macros.  

- pivot table  
HSSF doesn't have support for reading or creating Pivot tables. XSSF has limited support for creating Pivot Tables, and very limited read/change support.
