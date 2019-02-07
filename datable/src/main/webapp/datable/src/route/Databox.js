import React from "react";

class Databox extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {

        return (
            <div>
                Databox here
                {this.props.children}
            </div>
        );
    }
}

export default Databox;