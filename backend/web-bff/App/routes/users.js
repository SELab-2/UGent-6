/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

const express = require('express');
const router = express.Router();


const isAuthenticated = require('../util/isAuthenticated')

const { BACKEND_API_ENDPOINT, msalConfig, REDIRECT_URI} = require('../authConfig');
const authProvider = require("../auth/AuthProvider");

// custom middleware to check auth state


router.get('/id',
    isAuthenticated("/auth/signin"), // check if user is authenticated
    async function (req, res, next) {
        res.render('id', { idTokenClaims: req.session.account.idTokenClaims });
    }
);

router.get('/account',
    isAuthenticated("/users/not_signed_in"),
    async function (req, res, next) {
        res.send({
            isAuthenticated: true,
            account: {
                name: req.session.account.name
            }
        })
    }
);

router.get('/not_signed_in',
    async function(req, res, next) {
        res.send ({
          isAuthenticated: false,
          account: null
        })
    }
)

module.exports = router;