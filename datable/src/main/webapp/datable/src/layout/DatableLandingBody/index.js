import React from "react";

// import './index.scss';

class DatableLandingBody extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {

        return (
            <div className="container">
                {this.props.children}
            </div>
        );
    }
}

export default DatableLandingBody;