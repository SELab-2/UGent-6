/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

const express = require('express');
const authProvider = require("../auth/AuthProvider");
const router = express.Router();

/**
 * Index route for debugging purposes.
 *
 *  @route GET /
 */
router.get('/', function (req, res, next) {
    res.render('index', {
        title: 'MSAL Node & Express Web App',
        isAuthenticated: req.session.isAuthenticated,
        username: req.session.account?.username,
    });
});

/**
 * Index route that handles a correct login in the msal library.
 * This route must be /, this is configured in the application request.
 *
 *  @route POST /
 */
router.post('/', authProvider.handleRedirect());

module.exports = router;