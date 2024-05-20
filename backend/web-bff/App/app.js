require('dotenv').config({path:".env"});

const path = require('path');
const express = require('express');
const session = require('express-session');
const MongoStore = require('connect-mongo');
const createError = require('http-errors');
const logger = require('morgan');

const rateLimit = require('express-rate-limit')

const cors = require('cors')

const indexRouter = require('./routes/index');
const usersRouter = require('./routes/users');
const authRouter = require('./routes/auth');
const apiRouter = require('./routes/api');

/**
 * Initialize express
 */
const app = express();
const DEVELOPMENT = process.env.ENVIRONMENT === "development";

/**
 * Using cookie-session middleware for persistent user session.
 */
const connection_string = `mongodb://${process.env.DB_USER}:${process.env.DB_PASSWORD}@${process.env.DB_HOST}:${process.env.DB_PORT}/${process.env.DB_NAME}`


if (DEVELOPMENT) {
    // Use in memory storage for development purposes.
    // Keep in mind that when the server shuts down, so does the session information.
    app.use(session({
        name: 'pigeon session',
        secret: process.env.EXPRESS_SESSION_SECRET,
        resave: false,
        saveUninitialized: false,
        // expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days
        cookie: {
            httpOnly: true,
            secure: false, // make sure this is true in production
            maxAge: 7 * 24 * 60 * 60 * 1000,
        },
        //store: MongoStore.create(
        //    {mongoUrl: connection_string})

    }));
} else {
    // When using production mode, please make sure a mongodb instance is running and accepting connections
    // on port PORT. Also make sure the user exists.
    app.use(session({
        name: 'pigeon session',
        secret: process.env.EXPRESS_SESSION_SECRET,
        resave: false,
        saveUninitialized: false,
        // expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days
        cookie: {
            httpOnly: true,
            secure: true, // make sure this is true in production
            maxAge: 7 * 24 * 60 * 60 * 1000,
        },
        store: MongoStore.create(
            {mongoUrl: connection_string})
}));
}


/**
 * Initialize the rate limiter.
 *
 */
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 4000,
});

app.use(limiter);

/**
 * Initialize the cors protection.
 * Requests from our frontend are allowed.
 */
const corsOptions = {
    origin: [/localhost/, "https://sel2-6.ugent.be/"],
    optionsSuccessStatus: 200,
    credentials: true,
}
app.use('*', cors(corsOptions));


// view engine setup for debugging
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname, 'public')));

/**
 * Make our routes accessible.
 */
app.use('/', indexRouter);
app.use('/web/users', usersRouter);
app.use('/web/auth', authRouter);
app.use('/web/api', apiRouter)

/**
 * Catch 404 and forward to error handler.
 */
app.use(function (req, res, next) {
    next(createError(404));
});

/**
 * Error handler.
 */
app.use(function (err, req, res, next) {
    // set locals, only providing error in development
    res.locals.message = err.message;
    res.locals.error = DEVELOPMENT ? err : {};

    // render the error page
    res.status(err.status || 500);
    res.render('error');
});

module.exports = app;
