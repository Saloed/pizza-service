import React from 'react';
import { Admin, Resource } from 'react-admin';
import restProvider from 'ra-data-simple-rest';
import authProvider from './authProvider';
import { List, Datagrid, Edit, Create, SimpleForm, DateField, TextField, EditButton, DisabledInput, TextInput, LongTextInput, DateInput } from 'react-admin';
const PostTitle = ({  }) => {
    return <span>Post </span>;
};

export const Dummy = (props: any) => (
    <Edit title={props.toString()} {...props}>

    </Edit>
);

const dataProvider = restProvider('http://127.0.0.1:8080');
const App = () => <Admin dataProvider={dataProvider} authProvider={authProvider}>
    {(permissions: any)=> [
        <Resource name="dummy" list={Dummy(permissions)} />,
        permissions ? <Resource name="dummy" list={Dummy} /> : null,

    ]}
</Admin>;

export default App;
