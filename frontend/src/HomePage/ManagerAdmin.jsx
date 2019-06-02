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
    SelectField,
    Labeled,
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
import PromoCreateButton from "./PromoCreateButton";
import PromoFinishButton from "./PromoFinishButton";

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


const ConditionalPromoField = ({record, ...rest}) => {
    console.log(record)
    return record && record.promo && record.promo.id
        ? <Labeled label="Promo">
            <SelectField source={"promo.effect"} record={record} choices={[
                {id: 'DISCOUNT_5', name: 'Discount 5%'},
                {id: 'DISCOUNT_10', name: 'Discount 10%'},
                {id: 'DISCOUNT_15', name: 'Discount 15%'}
            ]} optionText="name" optionValue="id"/>
        </Labeled>
        : null;
};

function renderOrderIsPayed(record, source) {
    return <BooleanField record={{...record, Payed: !!(record.payment && record.payment.id)}} source={"Payed"}/>
}

const isPayedField = <FunctionField source="payment" label="Payed" render={renderOrderIsPayed}/>

const ManagerOrderShow = (props) => (
    <Show title={'Order: ' + props.id} actions={<ManagerOrderShowActions/>}{...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="status"/>
            {isPayedField}
            <ConditionalPromoField/>
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
            <TextInput source="password" type={"password"} validate={required()}/>
            <TextInput source="restaurant" validate={required()}/>
        </SimpleForm>
    </Create>
);


export const CourierCreate = (props) => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="username" validate={required()}/>
            <TextInput source="password" type={"password"} validate={required()}/>
        </SimpleForm>
    </Create>
);


export const OperatorCreate = (props) => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="username" validate={required()}/>
            <TextInput source="password" type={"password"} validate={required()}/>
            <NumberInput source="number" validate={required()}/>
        </SimpleForm>
    </Create>
);

const ManagerPromoList = (props) => (
    <List {...props} bulkActions={false}>
        <Datagrid rowClick="show">
            <TextField source="id"/>
            <TextField source="status"/>
            <SelectField source={"effect"} choices={[
                {id: 'DISCOUNT_5', name: 'Discount 5%'},
                {id: 'DISCOUNT_10', name: 'Discount 10%'},
                {id: 'DISCOUNT_15', name: 'Discount 15%'}
            ]} optionText="name" optionValue="id"/>
        </Datagrid>
    </List>
);




const startPromo = (promo) => () => {
    promo.status = 'ACTIVE'
    dataProvider(UPDATE, "promo", {
        data: promo,
        id: promo.id
    }).then(location.reload())
}


function getManagerPromoNewActions(promo) {
    return [
        <UiButton color="primary" onClick={startPromo(promo)}>Start</UiButton>
    ]
}

const finishPromo = (promo) => () => {
    promo.status = 'FINISHED'
    dataProvider(UPDATE, "promo", {
        data: promo,
        id: promo.id
    }).then(location.reload())
}


function getManagerPromoActiveActions(promo) {
    return [
        <UiButton color="primary" onClick={finishPromo(promo)}>Finish</UiButton>
    ]
}

function getManagerPromoFinishedActions(promo) {
    return [
        <PromoFinishButton promo={promo}/>
    ]
}

function getManagerPromoActions(promo) {
    console.log(promo)
    let actions = []
    if (promo.status === 'NEW') actions.push(...getManagerPromoNewActions(promo))
    if (promo.status === 'ACTIVE') actions.push(...getManagerPromoActiveActions(promo))
    if (promo.status === 'FINISHED') actions.push(...getManagerPromoFinishedActions(promo))
    return actions
}


class PromoActionButtons extends React.Component {
    render() {
        if (!this.props.data) return (null)
        return getManagerPromoActions(this.props.data);
    }
}

const ManagerPromoShowActions = ({basePath, data, resource}) => {
    return (
        <UiCardActions style={cardActionStyle}>
            <PromoActionButtons data={data}/>
        </UiCardActions>
    );
};

const ManagerPromoShow = (props) => (
    <Show title={'Promo'}  actions={<ManagerPromoShowActions/>}{...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="status"/>
            <SelectField source={"effect"} choices={[
                {id: 'DISCOUNT_5', name: 'Discount 5%'},
                {id: 'DISCOUNT_10', name: 'Discount 10%'},
                {id: 'DISCOUNT_15', name: 'Discount 15%'}
            ]} optionText="name" optionValue="id"/>
        </SimpleShowLayout>
    </Show>
);


const CreatePromoBulkActionButtons = (props) => (
    <Fragment>
        <PromoCreateButton {...props}/>
    </Fragment>
);

export const ManagerClientList = (props) => {
    return <List {...props} bulkActionButtons={<CreatePromoBulkActionButtons/>}>
        <Datagrid>
            <TextField source="login"/>
            <TextField source="address"/>
        </Datagrid>
    </List>;
};


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
                <Resource name={'promo'} list={ManagerPromoList} show={ManagerPromoShow}/>
                <Resource name={'pizza'} list={PizzaList} show={PizzaShow}/>
                <Resource name={'client'} list={ManagerClientList}/>
                <Resource name={'manager'} create={ManagerCreate} list={ManagerCreate}/>
                <Resource name={'operator'} create={OperatorCreate} list={OperatorCreate}/>
                <Resource name={'courier'} create={CourierCreate} list={CourierCreate}/>
            </Admin>
        </Provider>
    );
};
