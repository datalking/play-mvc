import React from "react";

import globalStyle from '../../common/style/style';

class TitleBar extends React.Component {

    static defaultProps = {
        title: '当前文件名',
    };

    handleSaveCurrentFile = e => {
        console.log('====handleSaveCurrentFile');
    }

    render() {

        const {
            title,
        } = this.props;

        const iconMidStyle = {
            float: 'left',
            display: 'table-cell',
            verticalAlign: 'middle',
        };
        const titleMidStyle = {
            float: 'left',
            textAlign: 'center',
            width: '84%',
            display: 'table-cell',
            verticalAlign: 'middle',
        };

        return (
            <div style={this.props}>
                <div style={iconMidStyle}>
                <span className="icon">
                  <i className="fa fa-th"></i>
                </span>
                    {/*<span className="icon" onClick={this.handleSaveCurrentFile}>*/}
                    {/*<i className="fa fa-save"></i>*/}
                    {/*</span>*/}
                </div>
                <div style={titleMidStyle}>
                    <span style={{lineHeight: '36px'}}>{title}</span>
                </div>
            </div>
        );
    }
}

export default TitleBar;
