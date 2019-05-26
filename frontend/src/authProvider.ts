import {AUTH_LOGIN, AUTH_LOGOUT, AUTH_ERROR, AUTH_GET_PERMISSIONS, AUTH_CHECK, fetchUtils} from 'react-admin';
import decodeJwt from 'jwt-decode';

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

class AuthToken {
    token: string;
    data: TokenData;

    constructor(token: string, data: TokenData) {
        this.token = token;
        this.data = data
    }

}

function storeToken(token: AuthToken) {
    localStorage.setItem('token', token.token);
    localStorage.setItem('authToken', JSON.stringify(token))
}

function getToken(): AuthToken | null {
    let data = localStorage.getItem('authToken');
    if (!data) return null;
    return JSON.parse(data)
}

function removeToken() {
    localStorage.removeItem('token');
    localStorage.removeItem('authToken');
}


export default (type: string, params: any) => {
    const paramSize = Object.keys(params).length
    if (paramSize > 2 && type === AUTH_LOGIN) {
        // registration
        const request = new Request('http://127.0.0.1:8080/client', {
            method: 'POST',
            body: JSON.stringify(params),
            headers: new Headers({'Content-Type': 'application/json'}),
        });
        return fetch(request)
            .then((response: Response) => {
                if (response.status < 200 || response.status >= 300) {
                    throw new Error(response.statusText);
                }
                return response.json();
            })
    }
    if (type === AUTH_LOGIN) {
        const {username, password} = params;
        const request = new Request('http://127.0.0.1:8080/authenticate', {
            method: 'POST',
            body: JSON.stringify({username, password}),
            headers: new Headers({'Content-Type': 'application/json'}),
        });
        return fetch(request)
            .then((response: Response) => {
                if (response.status < 200 || response.status >= 300) {
                    throw new Error(response.statusText);
                }
                return response.json();
            })
            .then((response: AuthResponse) => {
                let token = response.token;
                const decodedToken = decodeJwt<TokenData>(token);
                storeToken(new AuthToken(token, decodedToken))
            });
    }
    if (type === AUTH_LOGOUT) {
        removeToken();
        return Promise.resolve();
    }
    if (type === AUTH_ERROR) {
        const status = params.status;
        if (status === 401 || status === 403) {
            removeToken();
            return Promise.reject();
        }
        return Promise.resolve();
    }
    if (type === AUTH_CHECK) {
        return getToken() ? Promise.resolve() : Promise.reject();
    }
    if (type === AUTH_GET_PERMISSIONS) {
        const token = getToken();
        if (!token) return Promise.reject();
        const role = token.data.role;
        return role ? Promise.resolve(role) : Promise.reject();
    }
    return Promise.reject('Unknown method');
};


export const httpClient = (url: any, options: any = {}) => {
    if (!options.headers) {
        options.headers = new Headers({Accept: 'application/json'});
    }
    const token = localStorage.getItem('token');
    options.headers.set('Authorization', `Bearer ${token}`);
    return fetchUtils.fetchJson(url, options);
};
