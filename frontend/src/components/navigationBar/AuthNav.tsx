import { useAccount } from "@azure/msal-react"


const AuthNav = () => {
  const auth = useAccount()


  return <div>
    <span>{auth!.name}</span>
  </div>

}

export default AuthNav