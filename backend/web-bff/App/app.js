require('dotenv').config();

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

/* initialize express */
const app = express();


/**
 * Using cookie-session middleware for persistent user session.
 */

//connection_string = `mongodb://${process.env.DB_USER}:${process.env.DB_PASSWORD}@${process.env.DB_HOST}:${process.env.DB_PORT}/${process.env.DB_NAME}?authSource=admin`

//console.log(connection_string)

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
//    store: MongoStore.create(
//        {mongoUrl: connection_string})
}));


const limiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 4000,
});

app.use(limiter);

const corsOptions = {
    origin: /localhost/,
    optionsSuccessStatus: 200,
    credentials: true,
}

app.use('*', cors(corsOptions));

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname, 'public')));


app.use('/', indexRouter);
app.use('/web/users', usersRouter);
app.use('/web/auth', authRouter);
app.use('/web/api', apiRouter)

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
    // set locals, only providing error in development
    res.locals.message = err.message;
    res.locals.error = req.app.get('env') === 'development' ? err : {};

    // render the error page
    res.status(err.status || 500);
    res.render('error');
});

module.exports = app;
