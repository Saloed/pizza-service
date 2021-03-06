// in src/createAdminStore.js
import "regenerator-runtime/runtime";
import {applyMiddleware, combineReducers, compose, createStore} from 'redux';
import {routerMiddleware, routerReducer} from 'react-router-redux';
import {reducer as formReducer} from 'redux-form';
import createSagaMiddleware from 'redux-saga';
import {all, fork} from 'redux-saga/effects';
import {
    adminReducer,
    adminSaga,
    createAppReducer,
    defaultI18nProvider,
    i18nReducer,
    formMiddleware,
    USER_LOGOUT,
} from 'react-admin';


export default ({
                    authProvider,
                    dataProvider,
                    i18nProvider = defaultI18nProvider,
                    history,
                    locale = 'en',
                }) => {
    const reducer = combineReducers({
        admin: adminReducer,
        i18n: i18nReducer(locale, i18nProvider(locale)),
        form: formReducer,
        router: routerReducer
    });
    const resettableAppReducer = (state, action) =>
        reducer(action.type !== USER_LOGOUT ? state : undefined, action);

    const saga = function* rootSaga() {
        yield all(
            [
                adminSaga(dataProvider, authProvider, i18nProvider),
                // add your own sagas here
            ].map(fork)
        );
    };
    const sagaMiddleware = createSagaMiddleware();

    function configStore(){
        const store = createStore(
            resettableAppReducer,
            { /* set your initial state here */},
            compose(
                applyMiddleware(
                    sagaMiddleware,
                    formMiddleware,
                    routerMiddleware(history),
                    // add your own middlewares here
                ),
                typeof window !== 'undefined' && window.__REDUX_DEVTOOLS_EXTENSION__
                    ? window.__REDUX_DEVTOOLS_EXTENSION__()
                    : f => f
                // add your own enhancers here
            )
        );
        if (module.hot) {
            module.hot.accept('./createAdminStore', () => { store.replaceReducer(resettableAppReducer); });
        }
        return store
    }

    const store = configStore()
    sagaMiddleware.run(saga);
    return store;
};
