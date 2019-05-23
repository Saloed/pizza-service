import React from 'react';
import { Route } from 'react-router-dom';


import {ClientRegister} from './register'

export const customRoutes = [
    <Route exact path="/register" component={ClientRegister} />
];