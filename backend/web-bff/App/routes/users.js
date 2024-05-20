/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

const express = require('express');
const router = express.Router();


const isAuthenticated = require('../util/isAuthenticated')

const { BACKEND_API_ENDPOINT, msalConfig, REDIRECT_URI} = require('../authConfig');
const authProvider = require("../auth/AuthProvider");

/**
 * This route shows all id token claims for debugging purposes.
 *
 *  @route GET /web/users/id
 *
 *  Renders html page with id token claims.
 */
router.get('/id',
    isAuthenticated('/web/auth/signin'), // check if user is authenticated
    async function (req, res, next) {
        res.render('id', { idTokenClaims: req.session.account.idTokenClaims });
    }
);

/**
 * This route returns an object containing info about the authentication status.
 *
 *  @route GET /web/users/id
 *
 *  @returns
 *           isAuthenticated: boolean,
 *           account: {
 *               name: string
 *           }
 */
router.get('/isAuthenticated',

    async function (req, res, next) {
        try {
            if (req.session.isAuthenticated) {
                res.send({
                    isAuthenticated: true,
                    account: {
                        name: req.session.account?.name
                    }
                });
            } else {
                res.send({
                    isAuthenticated: false,
                    account: null
                })
            }
        } catch(error) {
            next(error);
        }
    }
);

/**
 * This route renders a page containing the accessToken for debugging purposes.
 *
 *  @route GET /web/users/token
 */
if (process.env.ENVIRONMENT === 'development') {
    router.get('/token',
        isAuthenticated('/web/auth/signin'),
        authProvider.acquireToken({
        scopes: [msalConfig.auth.clientId + "/.default"],
        redirectUri: REDIRECT_URI
        }),
        async function (req, res, next) {
            res.render('token', {accessToken: req.session.accessToken});
        }
    )
}



module.exports = router;