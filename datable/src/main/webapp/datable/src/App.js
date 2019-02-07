import React from 'react';

import LocaleLoader from "./component/LocaleLoader";
import routes from './route';

class App extends React.Component {

    state = {
        localeLoaded: false
    };

    loadLocale = () => {
        this.setState({
            localeLoaded: true
        });
    };

    render() {
        // console.log(this.state.localeLoaded)
        return (
            <LocaleLoader load={this.loadLocale}>
                {
                    this.state.localeLoaded && routes
                }
            </LocaleLoader>
        )
    }
}

export default App;
