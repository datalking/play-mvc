import React from "react";
import ReactSortableTree from '../../component/ReactSortableTree';
import FileTheme from '../../component/ReactSortableTreeFileTheme';
import {mockTableTemplateSidebarData} from '../../mock/mockComponentViewData';

// import './index.scss';

class TableTemplateSidebar extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            treeData: mockTableTemplateSidebarData(),
        };
    }

    render() {

        const tbTemplatesTreeData = this.state.treeData;
        // console.log(tbTemplatesTreeData)

        return (
            <div style={{height: 720, width: 450}}>
                <ReactSortableTree treeData={tbTemplatesTreeData}
                                   onChange={treeData => this.setState({treeData})}
                                   theme={FileTheme}
                                   canDrag={false}
                />
            </div>
        );
    }
}

export default TableTemplateSidebar;