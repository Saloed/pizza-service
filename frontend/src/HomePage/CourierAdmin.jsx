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
    BooleanField,
    NumberField,
    Labeled,
    SelectField,
    FunctionField,
    ArrayField,
    ReferenceManyField,
    SingleFieldList,
    ChipField,
    CREATE, UPDATE
} from 'react-admin';

import PaymentCreateButton from './PaymentCreateButton'

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

const shipOrder = (order) => () => {
    order.status = 'SHIPPING'
    dataProvider(UPDATE, "order", {
        data: order,
        id: order.id
    }).then(location.reload())
}
const closeOrder = (order) => () => {
    order.status = 'CLOSED'
    dataProvider(UPDATE, "order", {
        data: order,
        id: order.id
    }).then(location.replace('/#/order'))
}


function getOperatorOrderShippingActions(order) {
    if (!!(order.payment && order.payment.id)) {
        return [
            <UiButton color="primary" onClick={closeOrder(order)}>Close</UiButton>
        ]
    } else {
        return [
            <PaymentCreateButton order={order}/>
        ]
    }
}

function getCourierOrderReadyActions(order) {
    return [
        <UiButton color="primary" onClick={shipOrder(order)}>Deliver</UiButton>,
    ]
}


function getCourierOrderActions(order) {
    console.log(order)
    let actions = []
    if (order.status === 'READY') actions.push(...getCourierOrderReadyActions(order))
    if (order.status === 'SHIPPING') actions.push(...getOperatorOrderShippingActions(order))
    return actions
}

class ActionButtons extends React.Component {
    render() {
        if (!this.props.data) return (null)
        return getCourierOrderActions(this.props.data);
    }
}

const CourierOrderShowActions = ({basePath, data, resource}) => {
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

const CourierOrderShow = (props) => (
    <Show title={'Order: ' + props.id} actions={<CourierOrderShowActions/>}{...props}>
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

        </SimpleShowLayout>
    </Show>
);

const CourierOrderList = (props) => (
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



export const CourierAdmin = () => {
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
                title="Courier"
            >
                <Resource name={'order'} list={CourierOrderList} show={CourierOrderShow}/>
                <Resource name={'pizza'} list={PizzaList} show={PizzaShow}/>
                <Resource name={'promo'} list={ClientPromoList} show={ClientPromoShow}/>
            </Admin>
        </Provider>
    );
};
