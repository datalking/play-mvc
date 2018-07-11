# datable   
> power up data processing with excel-like experience.   

## target
- make it easy to handle two-dimensional data

## overview

## dev 
```sh
 git clone https://github.com/dataplaygrounds/datable.git
 cd datable/
 ./start-build-dev.sh
```

## demo
```sh
  cd datable/
  mvn tomcat7:run
```

start from [http://localhost:8999](http://localhost:8999)

## todo

- [ ] 支持 水平、竖直 无限滚动

- [ ] easyexcel不支持date类型的转换，可以借鉴xxl-excel的思想
- [ ] 添加书签：对行、列或单元格

- [ ] 实现Dropdown menu
- [ ] Export to file
- [ ] Filtering
- [ ] Header tooltips
- [ ] Nested headers
- [ ] Nested headers 排序
- [ ] Nested rows
- [ ] formula支持
- [ ] Gantt Chart


- [x] datable初始视图行默认显示64行，列默认显示28列

## later

- [ ] 使用ObserveChanges实现单项数据流

## License

[MIT](http://opensource.org/licenses/MIT)




