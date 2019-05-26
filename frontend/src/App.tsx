import React from 'react';
import {Admin, Resource} from 'react-admin';
import restProvider from 'ra-data-simple-rest';
import authProvider, {httpClient} from './authProvider';
import {ClientOrderList, ClientShow, ClientOrderShow, ClientOrderCreate} from './pages/client'
import {PizzaList} from "./pages/pizza";
import customRoutes from './customRoutes';

const dataProvider = restProvider('http://127.0.0.1:8080', httpClient);

const App = () => <Admin customRoutes={customRoutes} dataProvider={dataProvider} authProvider={authProvider}>
    <Resource name="client" show={ClientShow}/>
    <Resource name={'order'} list={ClientOrderList} show={ClientOrderShow} create={ClientOrderCreate}/>
    <Resource name={'pizza'} list={PizzaList}/>

</Admin>;

export default App;


// {(permissions: any) => [
//     permissions ? <Resource name="client" show={ClientShow}/> : null,
//
// ]}
