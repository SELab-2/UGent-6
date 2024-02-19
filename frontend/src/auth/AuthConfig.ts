import { Configuration, PopupRequest } from "@azure/msal-browser";

// Config object to be passed to Msal on creation
export const msalConfig: Configuration = {
    auth: {
        clientId: "39136cda-f02f-4305-9b08-45f132bab07e",
        authority:  "https://login.microsoftonline.com/62835335-e5c4-4d22-98f2-9d5b65a06d9d", //For UGent auth: "https://login.microsoftonline.com/d7811cde-ecef-496c-8f91-a1786241b99c",
        redirectUri: "/home",
        postLogoutRedirectUri: "/"
    },
    system: {
        allowNativeBroker: false // Disables WAM Broker
    },
    cache: {
        cacheLocation: "sessionStorage",
    }
};

// Add here scopes for id token to be used at MS Identity Platform endpoints.
export const loginRequest: PopupRequest = {
    scopes: ["User.Read"]
};