import React from 'react';
import {Route, Switch} from 'react-router';

import Workbook from './Workbook';
import NoMatch from './NoMatch';

const routes = (
    <div>
        {/*<NavBar />*/}
        <Switch>
            <Route exact path="/" component={Workbook}/>
            {/*<Route path="/hello" component={Hello} />*/}
            <Route component={NoMatch}/>
        </Switch>
    </div>
);

export default routes;
