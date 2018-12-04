import {combineReducers} from 'redux';
import {connectRouter} from 'connected-react-router'

import counterReducer from './counter';
import workbookReducer from './workbookReducer';

const rootReducer = (history) => combineReducers({
    router: connectRouter(history),
    count: counterReducer,
    workbook: workbookReducer,
});

export default rootReducer;
