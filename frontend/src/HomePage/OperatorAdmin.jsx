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
    ReferenceField,
    Labeled,
    SelectField,
    BooleanField,
    NumberField,
    FunctionField,
    ArrayField,
    ReferenceManyField,
    SingleFieldList,
    ChipField,
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

const approveOrder = (order) => () => {
    order.status = 'APPROVED'
    dataProvider(UPDATE, "order", {
        data: order,
        id: order.id
    }).then(location.replace('/#/order'))
}
const cancelOrder = (order) => () => {
    order.status = 'CANCELED'
    dataProvider(UPDATE, "order", {
        data: order,
        id: order.id
    }).then(location.replace('/#/order'))
}


function getOperatorOrderNewActions(order) {
    return [
        <UiButton color="primary" onClick={approveOrder(order)}>Approve</UiButton>,
        <UiButton color="primary" onClick={cancelOrder(order)}>Cancel</UiButton>
    ]
}

function getOperatorOrderActions(order) {
    console.log(order)
    let actions = []
    if (order.status === 'NEW') actions.push(...getOperatorOrderNewActions(order))
    return actions
}

class ActionButtons extends React.Component {
    render() {
        if (!this.props.data) return (null)
        return getOperatorOrderActions(this.props.data);
    }
}

const OperatorOrderShowActions = ({basePath, data, resource}) => {
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

const OperatorOrderShow = (props) => (
    <Show title={'Order: ' + props.id} actions={<OperatorOrderShowActions/>}{...props}>
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
        </SimpleShowLayout>
    </Show>
);

const OperatorOrderList = (props) => (
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


const ClientPromoList = (props) => (
    <List {...props} bulkActions={false}>
        <Datagrid rowClick="show">
            <SelectField source={"effect"} choices={[
                {id: 'DISCOUNT_5', name: 'Discount 5%'},
                {id: 'DISCOUNT_10', name: 'Discount 10%'},
                {id: 'DISCOUNT_15', name: 'Discount 15%'}
            ]} optionText="name" optionValue="id"/>
            <TextField source={"description"}/>
        </Datagrid>
    </List>
);


const ClientPromoShow = (props) => (
    <Show title={'Promo'} {...props}>
        <SimpleShowLayout>
            <SelectField source={"effect"} choices={[
                {id: 'DISCOUNT_5', name: 'Discount 5%'},
                {id: 'DISCOUNT_10', name: 'Discount 10%'},
                {id: 'DISCOUNT_15', name: 'Discount 15%'}
            ]} optionText="name" optionValue="id"/>
            <TextField source={"description"}/>
        </SimpleShowLayout>
    </Show>
);

const OperatorClientPromoList = (props) => (
    <List {...props} bulkActions={false}>
        <Datagrid rowClick="show">
            <TextField label={"Client"} source={"client.login"}/>
            <TextField source={"status"}/>
            <ReferenceField label="Promo" source="promoId" reference="promo" linkType={false}>
                <TextField source="description"/>
            </ReferenceField>
        </Datagrid>
    </List>
);


const informClient = (promoCLient) => () => {
    promoCLient.status = 'PROCESSING'
    dataProvider(UPDATE, "promoClient", {
        data: promoCLient,
        id: promoCLient.id
    }).then(location.reload())
}


const closeClient = (promoCLient) => () => {
    promoCLient.status = 'INFORMED'
    dataProvider(UPDATE, "promoClient", {
        data: promoCLient,
        id: promoCLient.id
    }).then(location.reload())
}


const reinformClient = (promoCLient) => () => {
    promoCLient.status = 'NOTINFORMED'
    dataProvider(UPDATE, "promoClient", {
        data: promoCLient,
        id: promoCLient.id
    }).then(location.reload())
}


function getOperatorOrderNotInformedActions(promoClient) {
    return [
        <UiButton color="primary" onClick={informClient(promoClient)}>Inform</UiButton>
    ]
}


function getOperatorOrderProcessingActions(promoClient) {
    return [
        <UiButton color="primary" onClick={closeClient(promoClient)}>Informed</UiButton>,
        <UiButton color="primary" onClick={reinformClient(promoClient)}>Not informed</UiButton>
    ]
}

function getOperatorPromoActions(promoClient) {
    let actions = []
    if (promoClient.status === 'NOTINFORMED') actions.push(...getOperatorOrderNotInformedActions(promoClient))
    if (promoClient.status === 'PROCESSING') actions.push(...getOperatorOrderProcessingActions(promoClient))
    return actions
}

class PromoActionButtons extends React.Component {
    render() {
        if (!this.props.data) return (null)
        return getOperatorPromoActions(this.props.data);
    }
}

const OperatorPromoShowActions = ({basePath, data, resource}) => {
    return (
        <UiCardActions style={cardActionStyle}>
            <PromoActionButtons data={data}/>
        </UiCardActions>
    );
};



const OperatorClientPromoShow = (props) => (
    <Show title={'Client promo'} actions={<OperatorPromoShowActions/>} {...props}>
        <SimpleShowLayout>
            <TextField label={"Client"} source={"client.login"}/>
            <TextField label={"Phone"} source={"client.phone"}/>
            <TextField label={"Address"} source={"client.address"}/>
            <TextField source={"status"}/>
            <ReferenceField label="Promo" source="promoId" reference="promo" linkType={false}>
                <SimpleShowLayout>
                    <SelectField source={"effect"} choices={[
                        {id: 'DISCOUNT_5', name: 'Discount 5%'},
                        {id: 'DISCOUNT_10', name: 'Discount 10%'},
                        {id: 'DISCOUNT_15', name: 'Discount 15%'}
                    ]} optionText="name" optionValue="id"/>
                    <TextField source="description"/>
                </SimpleShowLayout>
            </ReferenceField>
        </SimpleShowLayout>
    </Show>
);


export const OperatorAdmin = () => {
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
                title="Operator"
            >
                <Resource name={'order'} list={OperatorOrderList} show={OperatorOrderShow}/>
                <Resource name={'promoClient'} list={OperatorClientPromoList} show={OperatorClientPromoShow}/>
                <Resource name={'pizza'} list={PizzaList} show={PizzaShow}/>
                <Resource name={'promo'} list={ClientPromoList} show={ClientPromoShow}/>
            </Admin>
        </Provider>
    );
};
