import React, {Fragment} from "react";
import {Provider} from 'react-redux';
import {createHashHistory} from 'history';
import {Admin, Resource} from 'react-admin';
import defaultMessages from 'ra-language-english';

import createAdminStore from '../createAdminStore';
import {userService} from '../_services';


import {
    List,
    Datagrid,
    Show,
    SimpleShowLayout,
    TextField,
    BooleanField,
    CardActions,
    CreateButton,
    RefreshButton,
    ReferenceManyField,
    SingleFieldList,
    ChipField,
    Create,
    SimpleForm,
    ReferenceArrayInput,
    AutocompleteArrayInput
} from 'react-admin';


const ClientOrderListActions = ({
                                    bulkActions,
                                    basePath,
                                    currentSort,
                                    displayedFilters,
                                    exporter,
                                    filters,
                                    filterValues,
                                    onUnselectItems,
                                    resource,
                                    selectedIds,
                                    showFilter,
                                    total
                                }) => (
    <CardActions>
        <CreateButton basePath={basePath}/>
        <RefreshButton/>

    </CardActions>
)

const ClientOrderList = (props) => (
    <List {...props} bulkActions={false}>
        <Datagrid rowClick="show">
            <TextField source="id"/>
            <TextField source="status"/>
            <BooleanField source={"isPayed"}/>
        </Datagrid>
    </List>
);

const ClientOrderShow = (props) => (
    <Show title={'Order: ' + props.id} {...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="status"/>
            <BooleanField source={"isPayed"}/>
            <ReferenceManyField label={"Pizza"} reference={"pizza"} target={"orderId"}>
                <SingleFieldList>
                    <ChipField source="name"/>
                </SingleFieldList>
            </ReferenceManyField>

        </SimpleShowLayout>
    </Show>
);

const ClientShow = (props) => (
    <Show title={'Client'} {...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="login"/>
            <TextField source="address"/>
        </SimpleShowLayout>
    </Show>
);

export const ClientOrderCreate = (props) => (
    <Create {...props}>
        <SimpleForm>
            <ReferenceArrayInput label="Pizza" reference="pizza" source="pizza">
                <AutocompleteArrayInput/>
            </ReferenceArrayInput>
        </SimpleForm>
    </Create>
);


const CreateOrderBulkActionButtons = (props) => (
    <Fragment>
        {console.log(props)}
        <CreateButton label="New Order" {...props} />
        {/* Add the default bulk delete action */}
    </Fragment>
);

export const PizzaList = (props) => (
    <List {...props} bulkActionButtons={<CreateOrderBulkActionButtons/>
    }>
        <Datagrid>
            <TextField source="id"/>
            <TextField source="name"/>
        </Datagrid>
    </List>
);


// side effects
const authProvider = userService.restApiAuthProvider;
const dataProvider = userService.restApiDtaProvider();
const i18nProvider = locale => {
    return defaultMessages;
};
const history = createHashHistory();

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
                dataProvider={dataProvider}
                authProvider={authProvider}
                history={history}
                title="Client"
            >
                <Resource name={'order'} list={ClientOrderList} show={ClientOrderShow} create={ClientOrderCreate}/>
                <Resource name={'pizza'} list={PizzaList}/>
            </Admin>
        </Provider>
    );
};
