import {combineReducers} from 'redux';

import workbookReducer from './workbookReducer';

const rootReducer = combineReducers({
    workbook: workbookReducer,
});

export default rootReducer;
