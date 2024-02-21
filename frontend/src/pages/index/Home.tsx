import { AuthenticatedTemplate, UnauthenticatedTemplate } from "@azure/msal-react"
import { Link } from "react-router-dom"
import { msalInstance } from "../../index";

const Home = () => {

  const handleLogin = async () => {
    try {
       await msalInstance.loginPopup(); // Initiate popup login
    } catch (error) {
      console.error(error)
    }

  }

  return (
    <>
      <h1>HOME</h1>
      <AuthenticatedTemplate>
        <div style={{ display: "flex", flexDirection: "column" }}>
          <Link
            to="/profile"
            style={{ backgroundColor: "blue", color: "white", padding: "10px", textDecoration: "none", textAlign: "center" }}
          >
            Request Profile Information
          </Link>
        </div>
      </AuthenticatedTemplate>

      <UnauthenticatedTemplate>
        <div style={{textAlign:"center"}}>
        <p style={{ fontSize: "20px" }}> You're not signed in!</p>
        <button onClick={handleLogin}>Sign in with your microsoft account</button>

        </div>
      </UnauthenticatedTemplate>
    </>
  )
}

export default Home
