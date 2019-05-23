import {AUTH_LOGIN, AUTH_LOGOUT, AUTH_ERROR, AUTH_GET_PERMISSIONS, AUTH_CHECK} from 'react-admin';
import decodeJwt from 'jwt-decode';

interface AuthParams {
    username: string
    password: string
}

enum UserRole {
    Client = 'client',
    Manager = 'manager',
    Operator = 'operator',
    Courier = 'courier'
}

interface TokenData {
    id: number
    login: string
    role: UserRole
}

interface AuthResponse {
    token: string
}

export default (type: string, params: AuthParams) => {
    if (type === AUTH_LOGIN) {
        const {username, password} = params;
        const request = new Request('http://127.0.0.1:8080/authenticate', {
            method: 'POST',
            body: JSON.stringify({username, password}),
            headers: new Headers({'Content-Type': 'application/json'}),
        })
        return fetch(request)
            .then((response: Response) => {
                if (response.status < 200 || response.status >= 300) {
                    throw new Error(response.statusText);
                }
                return response.json();
            })
            .then((response: AuthResponse) => {
                console.log(response)
                let token = response.token
                const decodedToken = decodeJwt<TokenData>(token);
                localStorage.setItem('token', token);
                localStorage.setItem('role', decodedToken.role);
            });
    }
    if (type === AUTH_LOGOUT) {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        return Promise.resolve();
    }
    if (type === AUTH_ERROR) {
        // ...
    }
    if (type === AUTH_CHECK) {
        return localStorage.getItem('token') ? Promise.resolve() : Promise.reject();
    }
    if (type === AUTH_GET_PERMISSIONS) {
        const role = localStorage.getItem('role');
        return role ? Promise.resolve(role) : Promise.reject();
    }
    return Promise.reject('Unknown method');
};
