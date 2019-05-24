import React from 'react';
import Button from '@material-ui/core/Button';
import {
    List,
    Datagrid,
    Show,
    SimpleShowLayout,
    TextField,
    BooleanField,
    CardActions, CreateButton, ExportButton, RefreshButton,
    ReferenceManyField,
    SingleFieldList,
    ChipField,
    Create, Edit, SimpleForm, TextInput, DateInput, LongTextInput, DateField, EditButton,
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
                                }: ListActionArgument) => (
    <CardActions>
        <CreateButton basePath={basePath}/>
        <RefreshButton/>

    </CardActions>
)

export const ClientOrderList = (props: any) => (
    <List {...props} bulkActions={false}>
        <Datagrid rowClick="show">
            <TextField source="id"/>
            <TextField source="status"/>
            <BooleanField source={"isPayed"}/>
        </Datagrid>
    </List>
);

export const ClientOrderShow = (props: any) => (
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

export const ClientShow = (props: any) => (
    <Show title={'Client'} {...props}>
        <SimpleShowLayout>
            <TextField source="id"/>
            <TextField source="login"/>
            <TextField source="address"/>
        </SimpleShowLayout>
    </Show>
);

export const ClientOrderCreate = (props: any) => (
    <Create {...props}>
        <SimpleForm>
            <ReferenceArrayInput label="Pizza" reference="pizza" source="pizza">
                <AutocompleteArrayInput />
            </ReferenceArrayInput>
        </SimpleForm>
    </Create>
);
