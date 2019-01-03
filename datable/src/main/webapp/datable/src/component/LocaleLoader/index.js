import React, {Component, Fragment} from 'react';
import intl from 'react-intl-universal';
import PropTypes from 'prop-types';
import {SUPPORT_LOCALES, locales} from '../../common/constant';

export default class LocaleLoader extends Component {

    static propTypes = {
        load: PropTypes.func.isRequired
    };

    state = {
        currentLocale: localStorage.getItem('current_locale') || 'zh-CN'
    };

    componentDidMount() {
        this.loadLocales();
    }

    // locale初始化配置
    loadLocales = (value = this.state.currentLocale) => {
        intl.init({
            currentLocale: value,
            locales: locales
        })
            .then(() => this.props.load());
    };

    // 切换语言
    handleLocaleChange = value => {
        localStorage.setItem('current_locale', value);
        this.loadLocales(value);
    };

    render() {
        return (
            <Fragment>
                {/*<select defaultValue={this.state.currentLocale} onChange={this.handleLocaleChange}>*/}
                    {/*{*/}
                        {/*SUPPORT_LOCALES.length && SUPPORT_LOCALES.map(*/}
                            {/*(item, index) => <option key={index} value={item.value}>{item.name}</option>)*/}
                    {/*}*/}
                {/*</select>*/}
                <div>
                    {
                        this.props.children || null
                    }
                </div>
            </Fragment>
        );
    }
}