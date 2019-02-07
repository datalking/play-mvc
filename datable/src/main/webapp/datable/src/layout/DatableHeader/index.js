import React from "react";
import {Link, NavLink} from "react-router-dom";

import './index.scss';

class DatableHeader extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {

        return (
            <nav className="navbar is-white has-shadow" role="navigation" aria-label="main navigation">
                <div className="navbar-brand">
                    <div className="navbar-item">
                        {/*href="https://github.com/datalking/play-mvc/tree/master/datable/src/main/webapp/datable">*/}
                        <NavLink to="/" activeClassName="greenTextLogo">
                            {/*<img src="https://bulma.io/images/bulma-logo.png" width="112" height="28"/>*/}
                            {/*<span className="greenTextLogo"> Datable</span>*/}
                            Datable
                        </NavLink>
                    </div>

                    <a role="button" className="navbar-burger burger" aria-label="menu" aria-expanded="false"
                       data-target="navbarDatableHome">
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                    </a>
                </div>

                <div id="navbarDatableHome" className="navbar-menu">
                    <div className="navbar-start">
                        <div className="navbar-item">
                            <Link to="/table">
                                <span className="colorGray">New</span>
                            </Link>
                        </div>

                        <div className="navbar-item">
                            <Link to="/">
                                <span className="colorGray">Upload</span>
                            </Link>
                        </div>

                        {/*<div className="navbar-item has-dropdown is-hoverable">*/}
                        {/*<a className="navbar-link">*/}
                        {/*More*/}
                        {/*</a>*/}

                        {/*<div className="navbar-dropdown">*/}
                        {/*<a className="navbar-item">*/}
                        {/*About*/}
                        {/*</a>*/}
                        {/*<a className="navbar-item">*/}
                        {/*Jobs*/}
                        {/*</a>*/}
                        {/*</div>*/}
                        {/*</div>*/}
                    </div>

                    <div className="navbar-end">
                        <div className="navbar-item">
                            <div className="buttons">
                                <a className="button is-primary">
                                    <strong>Sign up</strong>
                                </a>
                                <a className="button is-light">
                                    Log in
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>
        );
    }
}

export default DatableHeader;