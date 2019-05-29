import React, {Component} from 'react'
import {Redirect} from 'react-router-dom'

export class LoginPageDummy extends Component {
    render() {
        return <Redirect to='/login'/>
    }
}
