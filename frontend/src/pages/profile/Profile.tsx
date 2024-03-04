import { useEffect, useState } from "react";

import { MsalAuthenticationTemplate, useMsal,MsalAuthenticationResult } from "@azure/msal-react";
import { InteractionStatus, InteractionType, InteractionRequiredAuthError, AccountInfo } from "@azure/msal-browser";
import { loginRequest } from "../../auth/AuthConfig";
import { ProfileData, GraphData } from "./components/ProfileData";
import { callMsGraph } from "../../auth/MsGraphApiCall";



const ErrorComponent: React.FC<MsalAuthenticationResult> = ({error}) => {
  return <h6>An Error Occurred: {error ? error.errorCode : "unknown error"}</h6>;
}


const ProfileContent = () => {
    const { instance, inProgress } = useMsal();
    const [graphData, setGraphData] = useState<null|GraphData>(null);

    useEffect(() => {
        if (!graphData && inProgress === InteractionStatus.None) {
            callMsGraph().then(response => setGraphData(response)).catch((e) => {
                if (e instanceof InteractionRequiredAuthError) {

                    instance.acquireTokenRedirect({
                        ...loginRequest,
                        account: instance.getActiveAccount() as AccountInfo
                    });
                }
            }).catch(err => {
                console.log(err);
            }) ;
        }
    }, [inProgress, graphData, instance]);
    
    
    console.log(graphData);
    return (
        <div>
            { graphData ? <ProfileData graphData={graphData} /> : null }
        </div>
    );
};

export function Profile() {



    const authRequest = {
        ...loginRequest
    };
    return (
        <MsalAuthenticationTemplate 
            interactionType={InteractionType.Redirect} 
            authenticationRequest={authRequest} 
            errorComponent={ErrorComponent} 
            loadingComponent={() => <h6>Authentication in progress...</h6>}
        >
            <ProfileContent />
        </MsalAuthenticationTemplate>
      )
};

export default Profile;