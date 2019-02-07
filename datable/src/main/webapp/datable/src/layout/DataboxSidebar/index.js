import React from "react";

// import './index.scss';

class DataboxSidebar extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {

        return (
            <div>
                数据盒子文件树型目录
                {this.props.children}
            </div>
        );
    }
}

export default DataboxSidebar;