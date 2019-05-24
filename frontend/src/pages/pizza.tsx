import React, {Component, Fragment} from "react";

import {BooleanField, Datagrid, List, TextField} from "react-admin";

import { Button, Confirm, crudCreate } from 'react-admin';import {
    CardActions,
    CreateButton,
    ExportButton,
    RefreshButton,
    DeleteButton,
    BulkActions
} from 'react-admin';


class ResetViewsButton extends Component {
    state = {
        isOpen: false,
    }

    handleClick = () => {
        this.setState({ isOpen: true });
    }

    handleDialogClose = () => {
        this.setState({ isOpen: false });
    };

    handleConfirm = () => {
        // @ts-ignore
        const { basePath, crudUpdateMany, resource, selectedIds } = this.props;
        crudCreate('order', { average_note: 10 }, 'order', 'order')
        this.setState({ isOpen: true });
    };

    render() {
        return (
            <Fragment>
                <Button label="New Order" onClick={this.handleClick} />
                <Confirm
                    isOpen={this.state.isOpen}
                    title="Create a new order"
                    content="Are you sure you want to create a new order for these pizzas?"
                    onConfirm={this.handleConfirm}
                    onClose={this.handleDialogClose}
                />
            </Fragment>
        );
    }
}



export const PizzaList = (props: any) => (
    <List {...props} bulkActionButtons={<ResetViewsButton/>
    }>
        <Datagrid>
            <TextField source="id"/>
            <TextField source="name"/>
        </Datagrid>
    </List>
);
