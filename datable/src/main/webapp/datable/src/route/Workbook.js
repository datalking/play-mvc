import React from 'react';

import TitleBar from '../component/TitleBar';
import RibbonMenu from '../component/RibbonMenu';
import CurrentTextarea from '../component/CurrentTextarea';
import HotTable from '../component/HotTable';
import {mockWorkbookDefaultData} from "../util/mock-data";

import 'font-awesome/css/font-awesome.css';
import 'bulma/css/bulma.css';
import 'handsontable/dist/handsontable.full.css'
import globalStyle from "../common/style/style";

class Workbook extends React.Component {

    constructor(props) {
        super(props);
        this.data = mockWorkbookDefaultData();
        // this.data = [
        //     ["", "Ford", "Volvo", "Toyota", "Honda"],
        //     ["2016", 10, 11, 12, 13],
        //     ["2017", 20, 11, 14, 13],
        //     ["2018", 30, 15, 12, 13]
        // ];
    }


    render() {

        const titleBarProps = {
            title: '你打开的文件名出现在这里hello',
            width: '100%',
            height: '36px',
            backgroundColor: globalStyle.xlsSkinGreen,
            color: '#fff',
            display: 'table',
        };
        const ribbonMenuProps = {
            height: '36px',
        };
        const currentTextareaProps = {
            height: '30px',
        };

        // 标题栏36、菜单栏120、公式栏30+8、指示器32、状态栏24
        const hotHeight = window.innerHeight - 36 - 120 - 38 - 32 - 24;
        console.log('====table高度：',hotHeight);
        const hotWidth = window.innerWidth-12;
        const hotContainerStyle = {
            width: hotWidth,
            height: hotHeight,
            overflow: 'hidden',
        };

        const hotConf = {
            data: this.data,
            colHeaders: true,
            rowHeaders: true,
            colWidths: 100,
            // rowHeights: 23,
            // stretchH: 'all',

        };
        const sheetIndicatorStyle = {
            backgroundColor: globalStyle.headerGray,
            height: '32px',
        };
        const statusBarStyle = {
            backgroundColor: globalStyle.cellGray,
            height: '24px',
        };


        return (
            <div>
                <TitleBar {...titleBarProps}/>

                <RibbonMenu {...ribbonMenuProps}/>

                <CurrentTextarea enableEdit={true} {...currentTextareaProps}/>

                <div style={hotContainerStyle}>
                    <HotTable {...hotConf} />
                </div>

                <div style={sheetIndicatorStyle}> sheet indicator</div>

                <div style={statusBarStyle}> status bar</div>
            </div>
        );
    }

}

export default Workbook;
