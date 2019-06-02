import React, {Component, Fragment} from 'react';
import {connect} from 'react-redux';
import {change, submit, isSubmitting} from 'redux-form';
import {
    fetchEnd,
    fetchStart,
    required,
    showNotification,
    crudGetMatching,
    Button,
    SaveButton,
    SimpleForm,
    TextInput,
    NumberInput,
    FormDataConsumer,
    SelectInput,
    LongTextInput,
    CREATE,
    REDUX_FORM_NAME
} from 'react-admin';
import IconContentAdd from '@material-ui/icons/Add';
import IconCancel from '@material-ui/icons/Cancel';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import {userService} from '../_services';

const dataProvider = userService.restApiDtaProvider();

class PromoCreateButton extends Component {
    constructor(props) {
        super(props);
    }

    state = {
        error: false,
        showDialog: false
    };

    handleClick = () => {
        this.setState({showDialog: true});
    };

    handleCloseClick = () => {
        this.setState({showDialog: false});
    };

    handleSaveClick = () =>{

        const {submit} = this.props;
        this.child.store.dispatch(submit("post-quick-create"))
        // Trigger a submit of our custom quick create form
        // This is needed because our modal action buttons are oustide the form
    };

    handleSubmit = values => {
        const {
            change,
            crudGetMatching,
            fetchStart,
            fetchEnd,
            showNotification
        } = this.props;

        // Dispatch an action letting react-admin know a API call is ongoing
        values.clientIds = this.props.selectedIds

        // As we want to know when the new post has been created in order to close the modal, we use the
        // dataProvider directly
        console.log(values)
        dataProvider(CREATE, 'promo', {data: values})
            .then(it => {
                this.setState({showDialog: false});
                location.replace(`/#/promo/${it.data.id}/show`)
            })
            .catch(error => {
                showNotification(error.message, 'error');
            });
    };

    render() {
        const {showDialog} = this.state;
        const {isSubmitting} = this.props;

        return (
            <Fragment>
                <Button onClick={this.handleClick} label="Promo">
                    <IconContentAdd/>
                </Button>
                <Dialog
                    fullWidth
                    open={showDialog}
                    onClose={this.handleCloseClick}
                    aria-label="Create promo"
                >
                    <DialogTitle>Create payment</DialogTitle>
                    <DialogContent>
                        <SimpleForm
                            ref={e => this.child = e}
                            // We override the redux-form name to avoid collision with the react-admin main form
                            form="post-quick-create"
                            resource="promo"
                            // We override the redux-form onSubmit prop to handle the submission ourselves
                            onSubmit={this.handleSubmit}
                            // We want no toolbar at all as we have our modal actions
                            toolbar={null}
                        >
                            <SelectInput source="effect" choices={[
                                {id: 'DISCOUNT_5', name: 'Discount 5%'},
                                {id: 'DISCOUNT_10', name: 'Discount 10%'},
                                {id: 'DISCOUNT_15', name: 'Discount 15%'}
                            ]} validate={required()}/>
                            <TextInput source={"description"} validate={required()}/>
                        </SimpleForm>
                    </DialogContent>
                    <DialogActions>
                        <SaveButton
                            saving={isSubmitting}
                            onClick={this.handleSaveClick}
                        />
                        <Button
                            label="ra.action.cancel"
                            onClick={this.handleCloseClick}
                        >
                            <IconCancel/>
                        </Button>
                    </DialogActions>
                </Dialog>
            </Fragment>
        );
    }
}

const mapStateToProps = state => {
    return ({
        isSubmitting: isSubmitting("post-quick-create")(state)

    });
};

const mapDispatchToProps = {
    change,
    crudGetMatching,
    fetchEnd,
    fetchStart,
    showNotification,
    submit
};

export default connect(mapStateToProps, mapDispatchToProps)(
    PromoCreateButton
);
