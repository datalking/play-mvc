import React from 'react';
import HotTable from '../HotTable';
import {mockWorkbookDefaultData} from "../../util/mock-data";

import globalStyle from '../../common/style/style';

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

        const hotContainerStyle = {
            width: '2400px',
            height: '640px',
            overflow: 'hidden',
        };
        const hotConf = {
            data: this.data,
            colHeaders: true,
            rowHeaders: true,
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
                <div style={hotContainerStyle}>
                    {/*<HotTable data={this.data} colHeaders={true} rowHeaders={true} stretchH="all"/>*/}
                    <HotTable {...hotConf} />
                </div>

                <div style={sheetIndicatorStyle}> sheet indicator</div>

                <div style={statusBarStyle}> status bar</div>
            </div>
        );
    }
}


export default Workbook;
