const busboy = require('busboy');
const FormData = require('form-data');
const fetch = require('../fetch');

function handleMultipart(req, res, next) {
    console.log("multipart")

    const bb = busboy({headers: req.headers});
    const form = new FormData();

    bb.on('file', (name, file, info) => {
        const {filename, encoding, mimetype} = info;
        file.on('data', (data) => {
            form.append(name, data, {filename, contentType: mimetype});
        });
    });

    bb.on('field', (fieldname, val) => {
        form.append(fieldname, val);
    });

    bb.on('close', async () => {
        try {
            const response = await fetch("api" + req.url, req.session.accessToken, req.method, form, form.getHeaders())
            res.status(response.code).send(response.data);
        } catch (error) {
            next(error);
        }
    });

    req.pipe(bb);
}

module.exports = handleMultipart;