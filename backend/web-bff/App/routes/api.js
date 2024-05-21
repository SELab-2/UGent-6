const authProvider = require('../auth/AuthProvider');

const express = require('express');
const router = express.Router();

const fetch = require('../fetch');

const { BACKEND_API_ENDPOINT, msalConfig, REDIRECT_URI} = require('../authConfig');
const isAuthenticated = require('../util/isAuthenticated');

/**
 *  Route that captures every method and route starting with /web/api.
 *  An access token is acquired and provided in the authorization header to the backend.
 *  The response is sent back to the frontend.
 *
 *  @route /web/api/*
 */
router.all('/*',
    isAuthenticated("/web/auth/signin"),
    authProvider.acquireToken({
    scopes: [msalConfig.auth.clientId + "/.default"],
    redirectUri: REDIRECT_URI
    }),
    async function(req, res, next) {

    try {
        const response = await fetch( "api" + req.url , req.session.accessToken, req.method, req.body, req.headers)
        res.status(response.code).send(response.data)
    } catch(error) {
        next(error);
    }
    })

module.exports = router;