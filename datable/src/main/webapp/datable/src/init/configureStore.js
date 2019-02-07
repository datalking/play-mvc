import {createStore, applyMiddleware, compose} from 'redux';
import createSagaMiddleware from 'redux-saga';
import rootReducer from '../reducer';

const sagaMiddleware = createSagaMiddleware();

const configureStore = preloadedState => {

    const store = createStore(
        rootReducer,
        preloadedState,
        compose(
            applyMiddleware(sagaMiddleware,),
            // DevTools.instrument()
        )
    );

    return store;
}

export default configureStore;