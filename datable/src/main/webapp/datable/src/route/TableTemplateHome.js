import React from "react";

class TableTemplateHome extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {

        return (
            <div>
                SheetTemplateHome here
                {this.props.children}
            </div>
        );
    }
}

export default TableTemplateHome;