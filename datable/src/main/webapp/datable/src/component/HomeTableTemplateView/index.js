import React from "react";

import {sliceArr} from "../../util/viewDataUtil";
import {Link} from "react-router-dom";

// import './index.scss';

class HomeTableTemplateView extends React.Component {

    constructor(props) {
        super(props);
    }

    static defaultProps = {
        templateNameArr: ['t1.xls', 't2.xlsx', 't3.xls', 't4.xlsx', 't5.xls', 't6.xls', 't7.xlsx', 't8.xls', 't9.xlsx', 't10.xls', 't11.xlsx'],
    }

    render() {
        const {templateNameArr} = this.props;
        const splittedArr = sliceArr(templateNameArr, 5);
        // console.log(splittedArr)
        return (
            <div className="marginTop2rem">
                <span className="is-6">New Spreadsheet From Template</span>
                <Link to="/templates"><span className="slateGrayColor fontSmall padLeft2rem">More</span></Link>
                <div>
                    {
                        splittedArr.map((subArr, index) => {

                            return (<div className="columns  pad2rem" key={index}>
                                {
                                    subArr.map((item, index2) => {
                                        return (
                                            <div className="column  has-text-centered " key={index2}>
                                                <figure className="image is-128x128">
                                                    <img src="https://bulma.io/images/placeholders/128x128.png"/>
                                                    <p>{item}</p>
                                                </figure>
                                            </div>
                                        )
                                    })
                                }
                            </div>)
                        })
                    }
                </div>
            </div>
        );
    }
}

export default HomeTableTemplateView;