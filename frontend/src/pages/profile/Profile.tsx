import { useEffect, useState } from "react";

import { MsalAuthenticationTemplate, useMsal,MsalAuthenticationResult } from "@azure/msal-react";
import { InteractionStatus, InteractionType, InteractionRequiredAuthError, AccountInfo } from "@azure/msal-browser";
import { loginRequest } from "../../auth/AuthConfig";
import { callMsGraph } from "../../auth/MsGraphApiCall";
import { Spin } from "antd";
import { User } from "../../providers/UserProvider"
import ProfileCard from "./components/ProfileCard"
import useUser from "../../hooks/useUser";

const ErrorComponent: React.FC<MsalAuthenticationResult> = ({error}) => {
  return <h6>An Error Occurred: {error ? error.errorCode : "unknown error"}</h6>;
}


const ProfileContent = () => {
    const { instance, inProgress } = useMsal();
    const [id, setId] = useState<String | null>(null);
    const [notuser, setNotuser] = useState<User | null>(null)
    const { user } = useUser()

    useEffect(() => {
        if (!id && inProgress === InteractionStatus.None) {
            callMsGraph().then(response => {
                    if (response) {
                        setId(response.id);
                    } else {
                        throw("User not found");
                    }
                }).catch((e) => {
                if (e instanceof InteractionRequiredAuthError) {

                    instance.acquireTokenRedirect({
                        ...loginRequest,
                        account: instance.getActiveAccount() as AccountInfo
                    });
                }
            }).catch(err => {
                console.log(err);
            });
            console.log(id);
        }
    }, [inProgress, id, instance]);

    useEffect(() => {
        if (!id) return

        //TODO: get request
        setTimeout(() => {
            setNotuser({
                course_url: "",
                projects_url: "",
                url: "",
                role: "student",
                email: "test@ugent.be",
                id: 1,
                name: "John",
                surname: "Doe"
            })
        }, 250)
    }, [id])

    if (user === null) {
        return (
          <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
            <Spin
              tip="Loading..."
              size="large"
            />
          </div>
        )
    }

    return (
        <div style={{padding: "10px 2rem"}}>
            <ProfileCard user={user} />
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