import React from 'react';
import ReactDOM from 'react-dom';
import {Provider} from 'react-redux';

import configureStore from './init/configureStore';
import App from './App';

// 导入全局通用样式，包括变量、字体图标
import './index.scss';

const store = configureStore();

ReactDOM.render((
    <Provider store={store}>
        <App/>
    </Provider>
), document.getElementById('root'));
