import React from 'react';
import { Route } from 'react-router-dom';
import EnhancedRegisterForm from './login/Register2';

export default [
    <Route exact path="/register" component={EnhancedRegisterForm} />,
];
