import { useEffect, useState } from "react";
import { Spin } from "antd";
import UserList from "./components/UserList"
import { ApiRoutes, GET_Responses, UserRole } from "../../@types/requests.d";

export type UserType = GET_Responses[ApiRoutes.USERS][number]

const ProfileContent = () => {
    const [users, setUsers] = useState<UserType[] | null>(null);

    function updateRole(user: UserType, role: UserRole) {
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
      setUsers([
        {
          userId: "1",
          name: "Alice",
          surname: "Kornelis",
          role: "student",
          url:  ApiRoutes.GROUP_MEMBER
        },
        {
          userId: "2",
          name: "Bob",
          surname: "Kornelis",
          role: "teacher",
          url:  ApiRoutes.GROUP_MEMBER
        },
        {
          userId: "3",
          name: "Charlie",
          surname: "Kornelis",
          role: "admin",
          url:  ApiRoutes.GROUP_MEMBER
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