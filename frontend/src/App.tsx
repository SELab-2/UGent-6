import React from 'react';
import { PublicClientApplication, AccountInfo } from "@azure/msal-browser";
import { msalConfig } from "./auth/AuthConfig";
const msalInstance = new PublicClientApplication(msalConfig);
let promise = msalInstance.initialize()


function App() {

  const handleLogin = async () => {
    try {
      await promise;
      await msalInstance.loginPopup(); // Initiate popup login
      const account: AccountInfo | null = msalInstance.getActiveAccount();

      

      console.log(account);
    } catch (error) {
      console.error(error);
    }
  };



  return (
    <div className="App">
      <header className="App-header">
      <button onClick={handleLogin}>Sign in with Microsoft</button>

      </header>
    </div>
  );
}

export default App;
