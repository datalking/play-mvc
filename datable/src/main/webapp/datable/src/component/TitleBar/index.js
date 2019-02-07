import React from "react";

import './index.scss';
import datableIconOnly from '../../common/image/datableIconOnly.png';

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

        return (
            <div className="TitleBar--default">
                {/*<div style={this.props}>*/}

                <div style={iconMidStyle}>

                    {/*<span className="icon">*/}
                    {/*<i className="fa fa-th"></i>*/}
                    {/*</span>*/}
                    <img src={datableIconOnly} alt={"datableIcon"} className="datableLogo"/>
                    {/*<span className="icon" onClick={this.handleSaveCurrentFile}>*/}
                    {/*<i className="fa fa-save"></i>*/}
                    {/*</span>*/}
                </div>

                <div className='TitleBar__title--default'>
                    <span className='TitleBar__text'>{title}</span>
                </div>
            </div>
        );
    }
}

export default TitleBar;
