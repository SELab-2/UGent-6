import { Configuration, PopupRequest } from "@azure/msal-browser"

const hostedOnServer = false && window.location.hostname === "sel2-6.ugent.be" // For now, gotta change this when ugent auth is fixed

// Config object to be passed to Msal on creation
export const msalConfig: Configuration = {
  auth: {
    clientId: hostedOnServer ? import.meta.env.VITE_UGENT_CLIENT_ID : import.meta.env.VITE_AUTH_CLIENT_ID,
    //For UGent auth: "https://login.microsoftonline.com/d7811cde-ecef-496c-8f91-a1786241b99c",
    authority: "https://login.microsoftonline.com/d7811cde-ecef-496c-8f91-a1786241b99c", //  "https://login.microsoftonline.com/62835335-e5c4-4d22-98f2-9d5b65a06d9d",
    redirectUri: "/",
    postLogoutRedirectUri: "/",
    
  },
  system: {
    allowNativeBroker: false,
  },
  cache: {
    cacheLocation: "localStorage",
  },
}

// Add here scopes for id token to be used at MS Identity Platform endpoints.
export const loginRequest: PopupRequest = {
  scopes: ["User.Read"],
}

// Add here the endpoints for MS Graph API services you would like to use.
export const graphConfig = {
  graphMeEndpoint: "https://graph.microsoft.com/v1.0/me",
}
