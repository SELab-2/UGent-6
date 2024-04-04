require('dotenv').config();

const path = require('path');
const express = require('express');
const session = require('cookie-session');
const createError = require('http-errors');
const cookieParser = require('cookie-parser');
const logger = require('morgan');
const helmet = require('helmet');
const hpp = require('hpp');
const csurf = require('csurf');
const rateLimit = require('express-rate-limit')

const indexRouter = require('./routes/index');
const usersRouter = require('./routes/users');
const authRouter = require('./routes/auth');

/* initialize express */
const app = express();

/* Set security configs */
app.use(helmet());
app.use(hpp());


/**
 * Using cookie-session middleware for persistent user session.
 */
app.use(session({
    name: 'session',
    secret: process.env.EXPRESS_SESSION_SECRET,
    expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days
}));

app.use(csurf(undefined));

const limiter = rateLimit({
   windowMs: 15 * 60 * 1000,
    max: 100,
});

app.use(limiter);

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(logger('dev'));
app.use(express.json());
app.use(cookieParser());
app.use(express.urlencoded({ extended: false }));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/auth', authRouter);

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