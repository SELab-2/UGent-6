/**
 * This route checks if the user is authenticated.
 * If not, the user is redirected to the supplied route.
 *
 * @param redirectPath supplied redirect route
 * @returns {(function(*, *, *): (*|undefined))|*}
 *
 * returns a function that takes 3 arguments: req, res, next to be used as express middleware.
 */

function isAuthenticated(redirectPath) {
    return (req, res, next) => {
        // If not authenticated, redirect
        if (!req.session.isAuthenticated) {
            return res.redirect(redirectPath);
        }
        // If authenticated, execute next function in middleware.
        next();
    }
}

module.exports = isAuthenticated;