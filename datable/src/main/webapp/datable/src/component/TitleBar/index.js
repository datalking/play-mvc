import React from "../../react";
// import React from "react";

// import {Tab, TabList, Tabs, TabPanel} from "../Tab";

import globalStyle from '../../common/style/style';

class TitleBar extends React.Component {

    static defaultProps = {
        title: '当前文件名',
    };


    styling() {
        const styleThis = {
            width: '100%',
            height: 36,
            // textAlign: 'center',
            // verticalAlign: 'middle',
            backgroundColor: globalStyle.xlsSkinGreen,
            color: '#fff',
            display:'table',
        };
        const styleTitleText = {
            lineHeight: '36px',
        };
        return {
            styleThis,
            styleTitleText,
        }
    }


    handleSaveCurrentFile=e=>{
        console.log('====handleSaveCurrentFile');
    }

    render() {

        const {
            title,
        } = this.props;

        const s = this.styling();

        return (
            <div style={s.styleThis}>
                <div style={{
                    float: 'left',
                    display:'table-cell',
                    verticalAlign:'middle',
                }}>
                <span className="icon">
                  <i className="fa fa-th"></i>
                </span>
                    <span className="icon" onClick={this.handleSaveCurrentFile}>
                  <i className="fa fa-save"></i>
                </span>
                </div>
                <div style={{
                    float: 'left',
                    textAlign: 'center',
                    width: '84%',
                    display:'table-cell',
                    verticalAlign:'middle',

                }}>
                    <span style={s.styleTitleText}>{title}</span>
                </div>
            </div>
        );
    }
}

export default TitleBar;
