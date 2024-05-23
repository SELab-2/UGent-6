import AppRouter from "./router/AppRouter"

import Layout from "./components/layout/nav/Layout"
import "./i18n/config"
import ThemeProvider from "./theme/ThemeProvider"
import { AppProvider } from "./providers/AppProvider"
import {AuthProvider} from "./providers/AuthProvider"
import { UserProvider } from "./providers/UserProvider"
import AppApiProvider from "./providers/AppApiProvider"
import ErrorProvider from "./providers/ErrorProvider"


function App() {


  return (
    <div className="App">
      <AppProvider>
        <ThemeProvider>
          <AppApiProvider>
            <AuthProvider>
              <UserProvider>
                <Layout>
                  <ErrorProvider>
                    <AppRouter />
                  </ErrorProvider>
                </Layout>
              </UserProvider>
            </AuthProvider>
          </AppApiProvider>
        </ThemeProvider>
      </AppProvider>
    </div>
  )
}

export default App
