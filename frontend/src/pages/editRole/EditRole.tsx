import { useEffect, useState } from "react";
import { Spin } from "antd";
import UserList from "./components/UserList"
import { ApiRoutes, GET_Responses, UserRole } from "../../@types/requests.d";
import apiCall from "../../util/apiFetch";

export type UsersType = GET_Responses[ApiRoutes.USERS]

const ProfileContent = () => {
    const [users, setUsers] = useState<UsersType[] | null>(null);

    function updateRole(user: UsersType, role: UserRole) {
      //TODO: PUT of PATCH call
      console.log("User: ", user);
      console.log("Role: ", role);
      if(!users) return;
      const updatedUsers = users.map((u) => {
        if (u.userId === user.userId) {
         return { ...u, role: role };
        }
        return u;
      });
      setUsers(updatedUsers);
    }

    useEffect(() => {
      //TODO: moet met GET call
        /*apiCall.get(ApiRoutes.USERS).then((res) => {
          console.log(res.data)
          setUsers(res.data)
        })*/
      setUsers([
        {
          userId: 1,
          name: "Alice Kornelis",
          role: "student",
          email: "test@test.test",
          url: "test"
        },
        {
          userId: 2,
          name: "Bob Kornelis",
          role: "teacher",
          email: "test@test.test",
          url: "test"
        },
        {
          userId: 3,
          name: "Charlie Kornelis",
          role: "admin",
          email: "test@test.test",
          url: "test"
        }
      ]);
      }, []);

    if (users === null) {
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
        <div style={{padding: "3rem"}}>
            <UserList users={users} updateRole={updateRole} />
        </div>
    );
};

export function EditRole() {
    return (
        <ProfileContent />
      )
};

export default EditRole;