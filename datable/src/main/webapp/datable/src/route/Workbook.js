import React from 'react';
import PropTypes from 'prop-types'
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';

import TitleBar from '../component/TitleBar';
import RibbonMenu from '../component/RibbonMenu';
import CurrentTextarea from '../component/CurrentTextarea';
import HotTable from '../component/HotTable';
import {mockWorkbookDefaultData} from "../util/mock-data";

import {updateSheetData, updateSheetReadOnly} from "../action/workbookAction";

import 'handsontable/dist/handsontable.full.css';
import 'font-awesome/css/font-awesome.css';
import 'bulma/css/bulma.css';
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
        this.hotInstanceRef = React.createRef();
    }

    onBeforeHotChange(changes, source) {
        // reduxStore.dispatch({
        //     type: 'updateData',
        //     dataChanges: changes
        // });

        this.props.actionUpdateSheetData(changes, source);

        return false;
    }

    toggleReadOnly = (event) => {
        console.log('==== 点击了只读checkbox')
        console.log(event)
        console.log(event.target.value)
        // reduxStore.dispatch({
        //     type: 'updateReadOnly',
        //     readOnly: event.target.checked
        // });

        this.props.actionUpdateSheetReadOnly(event.target.checked);
    }

    render() {

        console.log('====props Workbook');
        const {stateWorkbook} = this.props;

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
            setSheetReadOnly: this.toggleReadOnly,
            readOnlyState: stateWorkbook.settings.readOnly,
        };

        // 标题栏36、菜单栏120、公式栏30+8、指示器32、状态栏24
        const hotHeight = window.innerHeight - 36 - 120 - 38 - 32 - 24;
        console.log('====table高度：', hotHeight);
        const hotWidth = window.innerWidth - 12;
        const hotContainerStyle = {
            width: hotWidth,
            height: hotHeight,
            overflow: 'hidden',
        };

        const hotSetting = {
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
                    <HotTable ref={this.hotInstanceRef}
                              beforeChange={this.onBeforeHotChange}
                              settings={stateWorkbook.settings}/>
                </div>

                <div style={sheetIndicatorStyle}> sheet indicator</div>

                <div style={statusBarStyle}> status bar</div>
            </div>
        );
    }

}

// Workbook.propTypes = {
//     pathname: PropTypes.string,
//     search: PropTypes.string,
//     hash: PropTypes.string,
// }

const mapStateToProps = state => ({
    stateWorkbook: state.workbook,
    // search: state.router.location.search,
    // hash: state.router.location.hash,
})

const mapDispatchToProps = dispatch => ({
    actionUpdateSheetData: bindActionCreators(updateSheetData, dispatch),
    actionUpdateSheetReadOnly: bindActionCreators(updateSheetReadOnly, dispatch),
})

export default connect(mapStateToProps, mapDispatchToProps)(Workbook);

// export default Workbook;
