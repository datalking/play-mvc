import React from 'react';
import PropTypes from 'prop-types'
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';
import { withRouter } from 'react-router-dom'

import TitleBar from '../TitleBar';
import RibbonMenu from '../RibbonMenu';
import CurrentTextarea from '../CurrentTextarea';
import HotTable from '../HotTable';

import {updateSheetData, updateSheetReadOnly} from "../../action/workbookAction";

import globalStyle from "../../common/style/style";

class Workbook extends React.Component {

    constructor(props) {
        super(props);

        this.hotInstanceRef = React.createRef();
    }

    onBeforeHotChange = (changes, source) => {

        this.props.actionUpdateSheetData(changes, source);

        return false;
    }

    toggleReadOnly = (event) => {
        this.props.actionUpdateSheetReadOnly(event.target.checked);
    }

    render() {

        console.log('====props Workbook',this.props);
        const {stateWorkbook} = this.props;

        const titleBarProps = {
            title: '当前打开的表格文件test',
            // width: '100%',
            // height: '36px',
            // backgroundColor: globalStyle.xlsSkinGreen,
            // color: '#fff',
            // display: 'table',
        };
        const ribbonMenuProps = {
            setSheetReadOnly: this.toggleReadOnly,
            readOnlyState: stateWorkbook.settings.readOnly,
        };
        const currentTextareaProps = {
            height: '30px',
        };

        // 标题栏36、菜单栏120、公式栏30+8、指示器32、状态栏24
        const hotHeight = window.innerHeight - 36 - 120 - 38 - 32 - 24;
        // console.log('====table高度：', hotHeight);
        const hotWidth = window.innerWidth - 12-16;
        const hotContainerStyle = {
            width: hotWidth,
            height: hotHeight,
            overflow: 'hidden',
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
                <TitleBar {...titleBarProps} />

                <RibbonMenu {...ribbonMenuProps} />

                <CurrentTextarea enableEdit={true} {...currentTextareaProps} />

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
});

const mapDispatchToProps = dispatch => ({
    actionUpdateSheetData: bindActionCreators(updateSheetData, dispatch),
    actionUpdateSheetReadOnly: bindActionCreators(updateSheetReadOnly, dispatch),
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Workbook));

// export default Workbook;
