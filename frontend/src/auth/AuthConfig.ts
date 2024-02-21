import { Configuration, PopupRequest } from "@azure/msal-browser";

// Config object to be passed to Msal on creation
export const msalConfig: Configuration = {
    auth: {
        clientId: "39136cda-f02f-4305-9b08-45f132bab07e",
        //For UGent auth: "https://login.microsoftonline.com/d7811cde-ecef-496c-8f91-a1786241b99c",
        authority:  "https://login.microsoftonline.com/d7811cde-ecef-496c-8f91-a1786241b99c", 
        redirectUri: "/dashboard",
        postLogoutRedirectUri: "/"
    },
    system: {
        allowNativeBroker: false 
    },
    cache: {
        cacheLocation: "localStorage",
    }
}

// Add here scopes for id token to be used at MS Identity Platform endpoints.
export const loginRequest: PopupRequest = {
    scopes: ["User.Read"]
};

// Add here the endpoints for MS Graph API services you would like to use.
export const graphConfig = {
    graphMeEndpoint: "https://graph.microsoft.com/v1.0/me"
};