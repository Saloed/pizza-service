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

class PaymentCreateButton extends Component {
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
        fetchStart();
        console.log(values)


        // As we want to know when the new post has been created in order to close the modal, we use the
        // dataProvider directly
        dataProvider(CREATE, 'posts', {data: values})
            .then(({data}) => {
                // Refresh the choices of the ReferenceInput to ensure our newly created post
                // always appear, even after selecting another post
                crudGetMatching(
                    'posts',
                    'comments@post_id',
                    {page: 1, perPage: 25},
                    {field: 'id', order: 'DESC'},
                    {}
                );

                // Update the main react-admin form (in this case, the comments creation form)
                change(REDUX_FORM_NAME, 'post_id', data.id);
                this.setState({showDialog: false});
            })
            .catch(error => {
                showNotification(error.message, 'error');
            })
            .finally(() => {
                // Dispatch an action letting react-admin know a API call has ended
                fetchEnd();
            });
    };

    render() {
        const {showDialog} = this.state;
        const {isSubmitting} = this.props;

        return (
            <Fragment>
                <Button onClick={this.handleClick} label="Payment">
                    <IconContentAdd/>
                </Button>
                <Dialog
                    fullWidth
                    open={showDialog}
                    onClose={this.handleCloseClick}
                    aria-label="Create payment"
                >
                    <DialogTitle>Create payment</DialogTitle>
                    <DialogContent>
                        <SimpleForm
                            ref={e => this.child = e}
                            // We override the redux-form name to avoid collision with the react-admin main form
                            form="post-quick-create"
                            resource="payment"
                            // We override the redux-form onSubmit prop to handle the submission ourselves
                            onSubmit={this.handleSubmit}
                            // We want no toolbar at all as we have our modal actions
                            toolbar={null}
                        >
                            <NumberInput source={"amount"} validate={required()}/>
                            <SelectInput source="type" choices={[
                                {id: 'CASH', name: 'Cash'},
                                {id: 'CARD', name: 'Card'}
                            ]} validate={required()}/>
                            <FormDataConsumer>
                                {({ formData, ...rest }) => formData.type === 'CARD' &&
                                    <TextInput source={"transaction"} validate={required()} {...rest}/>
                                }
                            </FormDataConsumer>

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
    PaymentCreateButton
);
