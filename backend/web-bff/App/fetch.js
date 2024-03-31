/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

var axios = require('axios');
const https = require('https');
const {BACKEND_API_ENDPOINT} = require("./authConfig");


/**
 * Attaches a given access token to a Backend API Call
 * @param endpoint REST API endpoint to call
 * @param accessToken raw access token string
 */
async function fetch(endpoint, accessToken) {
    const url = new URL(endpoint, BACKEND_API_ENDPOINT)
    console.log(accessToken)
    const headers = {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
    }

    const config= {
        method: "GET",
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