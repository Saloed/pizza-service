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
    CREATE, UPDATE,
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

class PromoFinishButton extends Component {
    constructor(props) {
        super(props);
        this.promo = props.promo
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

    handleSaveClick = () => {

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

        // As we want to know when the new post has been created in order to close the modal, we use the
        // dataProvider directly
        this.promo.result = values.result
        this.promo.status = 'CLOSED'
        dataProvider(UPDATE, "promo", {
            data: this.promo,
            id: this.promo.id
        }).then(it => {
            this.setState({showDialog: false});
            location.reload()
        }).catch(error => {
            showNotification(error.message, 'error');
        });
    };

    render() {
        const {showDialog} = this.state;
        const {isSubmitting} = this.props;

        return (
            <Fragment>
                <Button onClick={this.handleClick} label="Close">
                    <IconContentAdd/>
                </Button>
                <Dialog
                    fullWidth
                    open={showDialog}
                    onClose={this.handleCloseClick}
                    aria-label="Close promo"
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
                            <TextInput source={"result"}  validate={required()} options={{ multiline: true }}/>
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
    PromoFinishButton
);
