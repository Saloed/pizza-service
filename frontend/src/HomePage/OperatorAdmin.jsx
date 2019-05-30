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

function renderOrderIsPayed(record, source) {
    return <BooleanField record={{...record, Payed: !!record.payment.id}} source={"Payed"}/>
}

const isPayedField = <FunctionField source="payment" label="Payed" render={renderOrderIsPayed}/>

const OperatorOrderShow = (props) => (
    <Show title={'Order: ' + props.id} actions={<OperatorOrderShowActions/>}{...props}>
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
                <Resource name={'pizza'} list={PizzaList} show={PizzaShow}/>
            </Admin>
        </Provider>
    );
};
