import React, {Fragment} from "react";
import {Provider} from 'react-redux';
import {createHashHistory} from 'history';
import {Admin, Resource} from 'react-admin';
import defaultMessages from 'ra-language-english';
import {LoginPageDummy} from './LoginLogoutDummy'
import createAdminStore from '../createAdminStore';
import {userService} from '../_services';
import UiCardActions from '@material-ui/core/CardActions'
import UiButton from '@material-ui/core/Button';

import {
    List,
    Datagrid,
    Show,
    Button,
    SimpleShowLayout,
    TextField,
    TextInput,
    NumberInput,
    SimpleForm,
    required,
    BooleanField,
    NumberField,
    FunctionField,
    ArrayField,
    ReferenceManyField,
    SingleFieldList,
    ChipField,
    Create,
    CREATE, UPDATE
} from 'react-admin';

// side effects
const authProvider = userService.restApiAuthProvider;
const dataProvider = userService.restApiDtaProvider();
const i18nProvider = locale => {
    return defaultMessages;
};
const history = createHashHistory();


const cardActionStyle = {
    zIndex: 2,
    display: 'inline-block',
    float: 'right',
};

const processOrder = (order) => () => {
    order.status = 'PROCESSING'
    dataProvider(UPDATE, "order", {
        data: order,
        id: order.id
    }).then(location.reload())
}
const readyOrder = (order) => () => {
    order.status = 'READY'
    dataProvider(UPDATE, "order", {
        data: order,
        id: order.id
    }).then(location.reload())
}
const cancelOrder = (order) => () => {
    order.status = 'CANCELED'
    dataProvider(UPDATE, "order", {
        data: order,
        id: order.id
    }).then(location.replace('/#/order'))
}


function getManagerOrderCancelActions(order) {
    return [
        <UiButton color="primary" onClick={cancelOrder(order)}>Cancel</UiButton>
    ]
}


function getManagerOrderApprovedActions(order) {
    return [
        <UiButton color="primary" onClick={processOrder(order)}>Process</UiButton>
    ]
}


function getManagerOrderProcessingActions(order) {
    return [
        <UiButton color="primary" onClick={readyOrder(order)}>Ready</UiButton>
    ]
}

function getManagerOrderActions(order) {
    console.log(order)
    let actions = []
    if (order.status === 'APPROVED') actions.push(...getManagerOrderApprovedActions(order))
    if (order.status === 'PROCESSING') actions.push(...getManagerOrderProcessingActions(order))
    const cancelStatus = [
        'APPROVED',
        'PROCESSING'
    ]
    if (cancelStatus.includes(order.status)) actions.push(...getManagerOrderCancelActions(order))
    return actions
}


class ActionButtons extends React.Component {
    render() {
        if (!this.props.data) return (null)
        return getManagerOrderActions(this.props.data);
    }
}

const ManagerOrderShowActions = ({basePath, data, resource}) => {
    return (
        <UiCardActions style={cardActionStyle}>
            <ActionButtons data={data}/>
        </UiCardActions>
    );
};

function renderOrderIsPayed(record, source) {
    return <BooleanField record={{...record, Payed: !!record.payment.id}} source={"Payed"}/>
}

const isPayedField = <FunctionField source="payment" label="Payed" render={renderOrderIsPayed}/>

const ManagerOrderShow = (props) => (
    <Show title={'Order: ' + props.id} actions={<ManagerOrderShowActions/>}{...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="status"/>
            {isPayedField}
            <NumberField source={"cost"}/>
            <TextField label={"Address"} source={"client.address"}/>
            <TextField label={"Phone"} source={"client.phone"}/>
            <TextField label={"Restaurant"} source={"manager.restaurant"}/>
            <ReferenceManyField label={"Pizza"} reference={"pizza"} target={"orderId"}>
                <SingleFieldList>
                    <ChipField source="name"/>
                </SingleFieldList>
            </ReferenceManyField>
            <TextField source={"operator.login"}/>
            <TextField source={"operator.number"}/>
            <TextField source={"manager.login"}/>
            <TextField source={"manager.restaurant"}/>
            <TextField source={"courier.login"}/>
        </SimpleShowLayout>
    </Show>
);

const ManagerOrderList = (props) => (
    <List {...props} bulkActions={false}>
        <Datagrid rowClick="show">
            <TextField source="id"/>
            <TextField source="status"/>
            {isPayedField}
            <NumberField source={"cost"}/>
        </Datagrid>
    </List>
);

const PizzaList = (props) => {
    return <List {...props} bulkActions={false}>
        <Datagrid>
            <TextField source="name"/>
            <ArrayField source="toppings">
                <SingleFieldList>
                    <ChipField source="name"/>
                </SingleFieldList>
            </ArrayField>
            <NumberField source={"price"}/>
        </Datagrid>
    </List>;
};

const PizzaShow = (props) => (
    <Show title={'Pizza'} {...props}>
        <SimpleShowLayout>
            <TextField source="name"/>
            <ArrayField source="toppings">
                <SingleFieldList>
                    <ChipField source="name"/>
                </SingleFieldList>
            </ArrayField>
            <NumberField source={"price"}/>
        </SimpleShowLayout>
    </Show>
);


export const ManagerCreate = (props) => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="username" validate={required()}/>
            <TextInput source="password"  type={"password"} validate={required()}/>
            <TextInput source="restaurant" validate={required()}/>
        </SimpleForm>
    </Create>
);


export const CourierCreate = (props) => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="username" validate={required()}/>
            <TextInput source="password"  type={"password"} validate={required()}/>
        </SimpleForm>
    </Create>
);


export const OperatorCreate = (props) => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="username" validate={required()}/>
            <TextInput source="password"  type={"password"} validate={required()}/>
            <NumberInput source="number" validate={required()}/>
        </SimpleForm>
    </Create>
);


export const ManagerAdmin = () => {
    return (
        <Provider
            store={createAdminStore({
                authProvider,
                dataProvider,
                i18nProvider,
                history,
            })}
        >
            <Admin
                loginPage={LoginPageDummy}
                dataProvider={dataProvider}
                authProvider={authProvider}
                history={history}
                title="Manager"
            >
                <Resource name={'order'} list={ManagerOrderList} show={ManagerOrderShow}/>
                <Resource name={'pizza'} list={PizzaList} show={PizzaShow}/>
                <Resource name={'manager'} create={ManagerCreate}  list={ManagerCreate}/>
                <Resource name={'operator'} create={OperatorCreate} list={OperatorCreate}/>
                <Resource name={'courier'} create={CourierCreate} list={CourierCreate}/>
            </Admin>
        </Provider>
    );
};
