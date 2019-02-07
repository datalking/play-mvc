import React from 'react';

import DatableHeader from "../layout/DatableHeader";
import DatableLandingBody from "../layout/DatableLandingBody";
import HomeRecentTableView from "../component/HomeRecentTableView";
import HomeTableTemplateView from "../component/HomeTableTemplateView";

class DatableHome extends React.Component {

    constructor(props) {
        super(props);
        // this.state = {
        //     boardData: ['aa', 'bb', 'cc', 'dd', 'ee', 'ff', 'gg', 'hh', 'iii', 'jj'],
        // }
    }

    render() {

        // const {boardData} = this.state;
        return (
            <div>
                <DatableHeader/>
                <DatableLandingBody>
                    <HomeRecentTableView/>
                    <HomeTableTemplateView/>
                </DatableLandingBody>
            </div>
        );
    }

}

export default DatableHome;
