import AppRouter from "./router/AppRouter"
import { IPublicClientApplication } from "@azure/msal-browser"
import { MsalProvider } from "@azure/msal-react"
import { useNavigate } from "react-router-dom"
import CustomNavigation from "./auth/CustomNavigation"
// const msalInstance = new PublicClientApplication(msalConfig);
// let promise = msalInstance.initialize()

type AppProps = {
  pca: IPublicClientApplication
}

function App({ pca }: AppProps) {

    const navigate = useNavigate();
    const navigationClient = new CustomNavigation(navigate);
    pca.setNavigationClient(navigationClient);


  return (
    <div className="App">
      <header className="App-header">       
          <MsalProvider instance={pca}>
            <AppRouter />
          </MsalProvider>
      </header>
    </div>
  )
}

export default App
