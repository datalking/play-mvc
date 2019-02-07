import React, {Component, Fragment} from 'react';
import intl from 'react-intl-universal';
import PropTypes from 'prop-types';
import {SUPPORT_LOCALES, locales} from '../../common/constant/langTranslationConstant';

export default class LocaleLoader extends Component {

    static propTypes = {
        load: PropTypes.func.isRequired
    };

    state = {
        currentLocale: localStorage.getItem('currentLocale') || 'zh-CN'
    };

    componentDidMount() {
        // console.log('==componentDidMount')

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
        localStorage.setItem('currentLocale', value);
        this.loadLocales(value);
    };

    render() {
        // console.log('==props4LocaleLoader', this.props)
        return this.props.children;

    }
}