import React from 'react';
import ReactDOM from 'react-dom';
import HotTable from '../HotTable';

class Workbook extends React.Component {

    constructor(props) {
        super(props);
        this.data = [
            ["", "Ford", "Volvo", "Toyota", "Honda"],
            ["2016", 10, 11, 12, 13],
            ["2017", 20, 11, 14, 13],
            ["2018", 30, 15, 12, 13]
        ];
    }

    render() {
        return (
            <HotTable data={this.data} colHeaders={true} rowHeaders={true} width="800" height="480" stretchH="all"/>
        );
    }
}


export default Workbook;
