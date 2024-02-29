import AppRouter from "./router/AppRouter"
import { IPublicClientApplication } from "@azure/msal-browser"
import { MsalProvider } from "@azure/msal-react"
import { useNavigate } from "react-router-dom"
import CustomNavigation from "./auth/CustomNavigation"
import Nav from "./components/navigationBar/Nav"
import './i18n/config';

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
            <Nav>
              <AppRouter />
            </Nav>
          </MsalProvider>
      </header>
    </div>
  )
}

export default App
