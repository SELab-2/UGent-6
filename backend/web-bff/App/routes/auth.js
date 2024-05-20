const express = require('express');

const authProvider = require('../auth/AuthProvider');
const { REDIRECT_URI, FRONTEND_URI, msalConfig} = require('../authConfig');

const router = express.Router();

/**
 * Route that starts the authentication flow for msal.
 *
 *  @route GET /web/auth/singin
 *
 *  On successful login the user is redirected to the frontend.
 */
router.get('/signin', authProvider.login({
    scopes: [msalConfig.auth.clientId + "/.default"],
    redirectUri: REDIRECT_URI,
    successRedirect: FRONTEND_URI,
}));

/**
 * No longer used. TODO: remove
 *
 * Route that acquires a token for accessing the backend resource server.
 * It stores this token in the session, it does not return the token.
 *
 *  @route GET /web/auth/acquireToken
 */
router.get('/acquireToken', authProvider.acquireToken({
    scopes: [msalConfig.auth.clientId + "/.default"],
    redirectUri: REDIRECT_URI
}));

/**
 * Route that starts the logout flow for msal.
 *
 *  @route GET /web/auth/signout
 */
router.get('/signout', authProvider.logout({
    postLogoutRedirectUri: FRONTEND_URI
}));

module.exports = router;