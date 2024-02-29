import { useState } from "react"
import { ApiRoutes } from "../../../@types/types"
import apiCall from "../../../util/apiFetch"

export type GraphData = {
  displayName: string
  jobTitle: string
  mail: string
  businessPhones: string[]
  officeLocation: string
}

export const ProfileData: React.FC<{ graphData: GraphData }> = ({ graphData }) => {
  const [apiResponse, setApiResponse] = useState<any>(null)

  const handleApiRequest = async () => {
    console.log("Making request...")
    apiCall.get(ApiRoutes.TEST).then(async (response) => {
      console.log(response.data)
      setApiResponse(response.data)
    })
  }

  

  return (
    <ul className="profileData">
      <li>
        <div>
          <div>
            <span>👤</span>
          </div>
          <div>
            <h3>Name</h3>
            <p>{graphData.displayName}</p>
          </div>
        </div>

        <div>
          <code></code>
          <pre>{apiResponse && JSON.stringify(apiResponse, null, 2)}</pre>
        </div>
      </li>

      <button onClick={handleApiRequest}>api request test</button>
    </ul>
  )
}
