import React, {Component} from 'react';
import {Route, Redirect} from 'react-router-dom';
import {userService} from '../_services/user.service'

export const PrivateRoute = ({component: Component, ...rest}) => (
    <Route {...rest} render={props => (
        localStorage.getItem('user')
            ? <Component {...props} />
            : <Redirect to={{pathname: '/login', state: {from: props.location}}}/>
    )}/>
);

export const PrivateRouteDispatcher = ({components: Components, ...rest}) => (
    <Route {...rest} render={props => {
        const userInfo = userService.userInfo();
        if (!userInfo) return <Redirect to={{pathname: '/login', state: {from: props.location}}}/>;
        const role = userInfo.role;
        const Comp = Components[role];
        return <Comp {...props} />

    }}/>
);

