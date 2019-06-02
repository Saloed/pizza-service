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
    Edit,
    Labeled,
    EditButton,
    ReferenceInput,
    SelectInput,
    ReferenceField,
    TextField,
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
import PromoSelectButton from "./PromoSelectButton";

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

const newOrder = (order) => () => {
    order.status = 'NEW'
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
    }).then(location.reload())
}


function getClientOrderNewActions(order) {
    return [
        <UiButton color="primary" onClick={newOrder(order)}>Approve</UiButton>,
        <PromoSelectButton order={order}/>
    ]
}

function getClientOrderCancelActions(order) {
    return [
        <UiButton color="primary" onClick={cancelOrder(order)}>Cancel</UiButton>,
    ]
}


function getClientOrderActions(order) {
    console.log(order)
    let actions = []
    if (order.status === 'DRAFT') actions.push(...getClientOrderNewActions(order))
    const cancelStatus = [
        'DRAFT',
        'NEW',
        'APPROVED',
        'PROCESSING',
        'READY',
        'SHIPPING'
    ]
    if (cancelStatus.includes(order.status)) actions.push(...getClientOrderCancelActions(order))
    return actions
}

class ActionButtons extends React.Component {
    render() {
        if (!this.props.data) return (null)
        return getClientOrderActions(this.props.data);
    }
}

const ClientOrderShowActions = ({basePath, data, resource}) => {
    return (
        <UiCardActions style={cardActionStyle}>
            <ActionButtons data={data}/>
        </UiCardActions>
    );
};

function renderOrderIsPayed(record, source) {
    return <BooleanField record={{...record, Payed: !!(record.payment && record.payment.id)}} source={"Payed"}/>
}


const isPayedField = <FunctionField source="payment" label="Payed" render={renderOrderIsPayed}/>

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

const ClientOrderShow = (props) => (
    <Show title={'Order: ' + props.id} actions={<ClientOrderShowActions/>}{...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="status"/>
            {isPayedField}
            <ConditionalPromoField/>
            <NumberField source={"cost"}/>
            <ReferenceManyField label={"Pizza"} reference={"pizza"} target={"orderId"}>
                <SingleFieldList>
                    <ChipField source="name"/>
                </SingleFieldList>
            </ReferenceManyField>

        </SimpleShowLayout>
    </Show>
);

const ClientOrderList = (props) => (
    <List {...props} bulkActions={false}>
        <Datagrid rowClick="show">
            <TextField source="id"/>
            <TextField source="status"/>
            {isPayedField}
            <NumberField source={"cost"}/>
        </Datagrid>
    </List>
);

class CreateOrderButton extends React.Component {
    handleAction = () => {
        const pizzaIds = this.props.selectedIds
        dataProvider(CREATE, "order", {data: {pizza: pizzaIds}}).then(it => location.replace(`/#/order/${it.data.id}/show`))
    };

    render() {
        return <Button variant="contained"
                       color="primary"
                       label={"New order"}
                       onClick={this.handleAction}
        />

    }
}

const CreateOrderBulkActionButtons = (props) => (
    <Fragment>
        <CreateOrderButton {...props}/>
    </Fragment>
);

export const PizzaList = (props) => {
    return <List {...props} bulkActionButtons={<CreateOrderBulkActionButtons/>}>
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


export const ClientAdmin = () => {
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
                title="Client"
            >
                <Resource name={'order'} list={ClientOrderList} show={ClientOrderShow}/>
                <Resource name={'pizza'} list={PizzaList} show={PizzaShow}/>
                <Resource name={'promo'} list={ClientPromoList} show={ClientPromoShow}/>
            </Admin>
        </Provider>
    );
};
