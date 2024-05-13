/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
const axios = require('axios');
const https = require('https');
const {BACKEND_API_ENDPOINT} = require("./authConfig");


/**
 * Attaches a given access token to a Backend API Call
 * @param endpoint REST API endpoint to call
 * @param accessToken raw access token string
 * @param method The http method for the call. Choice out of 'GET'
 */
async function fetch(endpoint, accessToken, method) {
    let methods = ["GET", "POST", "PATCH", "PUT", "DELETE"]
    if (!(methods.includes(method))) {
        throw new Error('Not a valid HTTP method');
    }
    const url = new URL(endpoint, BACKEND_API_ENDPOINT)
    const headers = {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json",
    }

    const config= {
        method: method,
        url: url.toString(),
        headers: headers,
    }

    console.log(`request made to ${BACKEND_API_ENDPOINT}/${endpoint} at: ` + new Date().toString());

    try {

        const response = await axios(config);
        return await response.data;
    } catch (error) {
        throw new Error(error);
    }
}

module.exports = fetch;
