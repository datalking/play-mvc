import React from 'react';
import TableTemplateSidebar from '../layout/TableTemplateSidebar';
import Sidebar from '../component/Sidebar';
import Workbook from '../component/Workbook';

class TablePage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            sidebarOpen: false,
        };
    }

    onSetSidebarOpen = (open) => {
        this.setState({sidebarOpen: open});
    }

    render() {

        return (
            <div>
                <Sidebar
                    sidebar={<TableTemplateSidebar/>}
                    open={this.state.sidebarOpen}
                    onSetOpen={this.onSetSidebarOpen}
                    styles={{sidebar: {background: "white"}}}
                >
                    <Workbook onClick={() => this.onSetSidebarOpen(true)}>
                    </Workbook>
                </Sidebar>
            </div>
        )
    }

}

export default TablePage;
