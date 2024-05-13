import AppRouter from "./router/AppRouter"
import { IPublicClientApplication } from "@azure/msal-browser"
import { MsalProvider } from "@azure/msal-react"
import { useNavigate } from "react-router-dom"
import CustomNavigation from "./auth/CustomNavigation"
import Layout from "./components/layout/nav/Layout"
import "./i18n/config"
import ThemeProvider from "./theme/ThemeProvider"
import { AppProvider } from "./providers/AppProvider"
import { UserProvider } from "./providers/UserProvider"
import AppApiProvider from "./providers/AppApiProvider"
import ErrorProvider from "./providers/ErrorProvider"

type AppProps = {
  pca: IPublicClientApplication
}

function App({ pca }: AppProps) {
  const navigate = useNavigate()
  const navigationClient = new CustomNavigation(navigate)
  pca.setNavigationClient(navigationClient)

  return (
    <div className="App">
      <AppProvider>
        <ThemeProvider>
          <AppApiProvider>
            <MsalProvider instance={pca}>
              <UserProvider>
                <Layout>
                  <ErrorProvider>
                    <AppRouter />
                  </ErrorProvider>
                </Layout>
              </UserProvider>
            </MsalProvider>
          </AppApiProvider>
        </ThemeProvider>
      </AppProvider>
    </div>
  )
}

export default App
