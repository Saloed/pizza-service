import config from 'config';
import decodeJwt from 'jwt-decode';
import {authHeader} from '../_helpers';
import restProvider from 'ra-data-simple-rest';
import {AUTH_CHECK, AUTH_ERROR, AUTH_GET_PERMISSIONS, AUTH_LOGIN, AUTH_LOGOUT, HttpError} from 'react-admin'
import axios from 'axios'

export const userService = {
    login,
    logout,
    userInfo,
    register,
    getAll,
    restApiDtaProvider,
    restApiAuthProvider,
    getById,
    update,
    delete: _delete
};

function login(username, password) {
    const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    };
    return fetch(`${config.apiUrl}/authenticate`, requestOptions)
        .then(handleResponse)
        .then(user => {
            // store user details and jwt token in local storage to keep user logged in between page refreshes
            const decodedToken = decodeJwt(user.token);
            localStorage.setItem('user', JSON.stringify(user));
            localStorage.setItem('userInfo', JSON.stringify(decodedToken));
            return user;
        });
}

function logout() {
    // remove user from local storage to log user out
    localStorage.removeItem('user');
    localStorage.removeItem('userInfo')
}

function userInfo() {
    const userInfo = localStorage.getItem("userInfo");
    if (!userInfo) return null;
    return JSON.parse(userInfo)
}

function getAll() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/users`, requestOptions).then(handleResponse);
}

function getById(id) {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/users/${id}`, requestOptions).then(handleResponse);
}

function register(user) {
    const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(user)
    };

    return fetch(`${config.apiUrl}/client`, requestOptions).then(handleResponse);
}

function update(user) {
    const requestOptions = {
        method: 'PUT',
        headers: {...authHeader(), 'Content-Type': 'application/json'},
        body: JSON.stringify(user)
    };

    return fetch(`${config.apiUrl}/users/${user.id}`, requestOptions).then(handleResponse);
}

// prefixed function name with underscore because delete is a reserved word in javascript
function _delete(id) {
    const requestOptions = {
        method: 'DELETE',
        headers: authHeader()
    };

    return fetch(`${config.apiUrl}/users/${id}`, requestOptions).then(handleResponse);
}

function handleResponse(response) {
    return response.text().then(text => {
        const data = text && JSON.parse(text);
        if (!response.ok) {
            if (response.status === 401) {
                // auto logout if 401 response returned from api
                logout();
                location.reload(true);
            }

            const error = (data && data.message) || response.statusText;
            return Promise.reject(error);
        }

        return data;
    });
}


const fetchJson = (url, options) => {
    return axios(url, {...options, data: options.body})
        .then((response) => {
            console.log(response)
            if (response.status < 200 || response.status >= 300) {
                return Promise.reject(new HttpError(
                    (response.data && response.data.message) || response.statusText,
                    response.status,
                    response.data
                ));
            }
            const headers = new Headers({...response.headers})
            return Promise.resolve({
                json: response.data,
                headers: headers,
                status: response.status,
                statusText: response.statusText
            });
        })
        .catch(error => {
            const response = error.response
            return Promise.reject(new HttpError(
                (response.data && response.data.message) || response.statusText,
                response.status,
                response.data
            ));
        });
};

const httpClient = (url, options) => {
    let headers = options.headers;
    if (!headers) {
        headers = new Headers();
    }
    headers.Accept = 'application/json'
    headers["Content-Type"] = 'application/json'

    const authHeaders = authHeader();
    for (const k in authHeaders) {
        headers[k] = authHeaders[k];
    }
    options.headers = headers;
    return fetchJson(url, options);
};

function restApiDtaProvider() {
    return restProvider(config.apiUrl, httpClient);
}

function restApiAuthProvider(type, params) {
    console.log(type, params);
    if (type === AUTH_LOGOUT) {
        logout();
        return Promise.resolve();
    }
    if (type === AUTH_ERROR) {
        const status = params.status;
        if (status === 401 || status === 403) {
            logout();
            return Promise.reject();
        }
        return Promise.resolve();
    }
    if (type === AUTH_LOGIN || type === AUTH_CHECK || type === AUTH_GET_PERMISSIONS) {
        return userInfo() ? Promise.resolve() : Promise.reject();
    }
    return Promise.reject('Unknown method');
}


