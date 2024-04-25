const express = require('express');
const router = express.Router();

const fetch = require('../fetch');

const { BACKEND_API_ENDPOINT } = require('../authConfig');

// custom middleware to check auth state
function isAuthenticated(req, res, next) {
    if (!req.session.isAuthenticated) {
        return res.redirect('/auth/signin'); // redirect to sign-in route
    }

    next();
}

router.all('/*',
    isAuthenticated,
    async function(req, res, next) {
    try {
        const response = await fetch(req.url , req.session.accessToken, req.method)
        res.send(response)
    } catch(error) {
        next(error);
    }
    })