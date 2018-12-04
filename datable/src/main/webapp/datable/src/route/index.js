import React from 'react';
import {Route, Switch} from 'react-router';

import Workbook from './Workbook';
import NoMatch from './NoMatch'
// import Hello from '../components/Hello'
// import Counter from '../components/Counter'
// import NavBar from '../components/NavBar'

const routes = (
    <div>
        {/*<NavBar />*/}
        <Switch>
            <Route exact path="/" component={Workbook}/>
            {/*<Route path="/hello" component={Hello} />*/}
            {/*<Route path="/counter" component={Counter} />*/}
            <Route component={NoMatch}/>
        </Switch>
    </div>
);

export default routes;
