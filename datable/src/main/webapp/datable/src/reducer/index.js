import {combineReducers} from 'redux';
import {connectRouter} from 'connected-react-router'

import workbookReducer from './workbookReducer';

const rootReducer = (history) => combineReducers({
    router: connectRouter(history),
    workbook: workbookReducer,
});

export default rootReducer;
