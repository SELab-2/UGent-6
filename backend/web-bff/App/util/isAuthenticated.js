

function isAuthenticated(redirectPath) {
    return (req, res, next) => {
        if (!req.session.isAuthenticated) {
            return res.redirect(redirectPath); // redirect
        }

        next();
    }
}

module.exports = isAuthenticated;