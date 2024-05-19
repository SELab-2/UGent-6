/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

const express = require('express');
const router = express.Router();

const fetch = require('../fetch');

const { BACKEND_API_ENDPOINT, msalConfig, REDIRECT_URI} = require('../authConfig');
const authProvider = require("../auth/AuthProvider");

// custom middleware to check auth state
function isAuthenticated(req, res, next) {
    if (!req.session.isAuthenticated) {
        return res.redirect('/web/auth/signin'); // redirect to sign-in route
    }

    next();
}

router.get('/id',
    isAuthenticated, // check if user is authenticated
    async function (req, res, next) {
        res.render('id', { idTokenClaims: req.session.account.idTokenClaims });
    }
);

router.get('/isAuthenticated',

    async function (req, res, next) {
        try {
            if (req.session.isAuthenticated) {
                res.send({
                    isAuthenticated: true,
                    name: req.session.account.name
                });
            } else {
                res.send({
                    isAuthenticated: false,
                    name: ""
                })
            }
        } catch(error) {
            next(error);
        }
    }
);

module.exports = router;