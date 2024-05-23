FROM node:21-bookworm

WORKDIR /express-web-bff

COPY App/package*.json ./

RUN npm install

COPY App/ .

CMD npm start
