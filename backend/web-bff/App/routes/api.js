const authProvider = require('../auth/AuthProvider');

const express = require('express');
const router = express.Router();

const fetch = require('../fetch');

const { BACKEND_API_ENDPOINT, msalConfig, REDIRECT_URI} = require('../authConfig');

// custom middleware to check auth state
function isAuthenticated(req, res, next) {
    if (!req.session.isAuthenticated) {
        return res.redirect('/web/auth/signin'); // redirect to sign-in route
    }

    next();
}

router.all('/*',
    isAuthenticated,
    authProvider.acquireToken({
    scopes: [msalConfig.auth.clientId + "/.default"],
    redirectUri: REDIRECT_URI
    }),
    async function(req, res, next) {

    try {
        const response = await fetch( "api" + req.url , req.session.accessToken, req.method)
        res.send(response)
    } catch(error) {
        next(error);
    }
    })

module.exports = router;