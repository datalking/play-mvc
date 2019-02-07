import React from "react";
import {Link} from "react-router-dom";

// import './index.scss';

class HomeRecentTableView extends React.Component {

    static defaultProps = {
        fileNameArr: ['aa.xls', 'bb.xlsx', 'cc.xls', 'dd.xlsx', 'ee.xls',]
    }

    render() {
        const {fileNameArr} = this.props;
        return (
            <div className="marginTop2rem">
                <span className="is-6">Open Recent Spreadsheet</span>
                <Link to="/databox"><span className="slateGrayColor fontSmall padLeft2rem">More</span></Link>
                <div className="columns pad2rem">
                    {
                        fileNameArr.map((item, index) => {
                            return (
                                <div className="column has-text-centered" key={index}>
                                    <figure className="image is-128x128">
                                        <img src="https://bulma.io/images/placeholders/128x128.png"/>
                                        <p>{item}</p>
                                    </figure>
                                </div>
                            )
                        })
                    }
                </div>
            </div>
        );
    }
}

export default HomeRecentTableView;