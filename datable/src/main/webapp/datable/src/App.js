import React from 'react';
import PropTypes from 'prop-types';
import {ConnectedRouter} from 'connected-react-router';

import routes from './route';
import LocaleLoader from "./component/LocaleLoader";

class App extends React.Component {

    static propTypes = {
        history: PropTypes.object,
    };

    constructor(props) {
        super(props);
    }

    state = {
        localeLoaded: false
    };

    loadLocale = () => {
        this.setState({
            localeLoaded: true
        });
    };

    render() {
        const {history} = this.props;
        return (
            <LocaleLoader load={this.loadLocale}>
                {
                    this.state.localeLoaded
                    &&
                    (< ConnectedRouter history={history}>
                        {routes}
                    </ConnectedRouter>)
                }
            </LocaleLoader>
        )
    }
}

export default App;
