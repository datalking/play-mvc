import React from 'react';
import {BrowserRouter as Router, Switch, Route, Redirect} from 'react-router-dom';

import DatableHome from './DatableHome';
import NoMatch from './NoMatch';
import TableTemplateHome from "./TableTemplateHome";
import Databox from "./Databox";
import TablePage from "./TablePage";

const routes = (
    <Router>
        {/*<NavBar />*/}
        <Switch>
            <Route exact path="/" component={DatableHome}/>
            <Route path="/table" component={TablePage}/>
            <Route path="/templates" component={TableTemplateHome}/>
            <Route path="/databox" component={Databox}/>
            <Route component={NoMatch}/>
        </Switch>
    </Router>

);

export default routes;
