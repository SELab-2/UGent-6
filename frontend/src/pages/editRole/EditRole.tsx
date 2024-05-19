import { useEffect, useState, useRef } from "react"
import { Row, Col, Form, Input, Button, Spin, Select, Typography } from "antd"
import UserList from "./components/UserList"
import { ApiRoutes, GET_Responses, UserRole } from "../../@types/requests.d"
import apiCall from "../../util/apiFetch"
import { useTranslation } from "react-i18next"
import { UsersListItem } from "./components/UserList"
import { useDebounceValue } from "usehooks-ts"
import { User } from "../../providers/UserProvider"

export type UsersType = GET_Responses[ApiRoutes.USERS]
type SearchType = "name" | "surname" | "email"
const ProfileContent = () => {
  const [users, setUsers] = useState<UsersType | null>(null)

  const [loading, setLoading] = useState(false)
  const [form] = Form.useForm()
  const searchValue = Form.useWatch("search", form)
  const [debouncedSearchValue] = useDebounceValue(searchValue, 250)
  const [searchType, setSearchType] = useState<SearchType>("name")

  const { t } = useTranslation()

  useEffect(() => {
    onSearch()
  }, [debouncedSearchValue])

  function updateRole(user: UsersListItem, role: UserRole) {
    console.log(user, role)
    apiCall.patch(ApiRoutes.USER, { role: role }, { id: user.id }).then((res) => {
      console.log(res.data)
      //onSearch();
      //replace this user in the userlist with the updated one from res.data
      const updatedUsers = users?.map((u) => {
        if (u.id === user.id) {
          return { ...u, role: res.data.role };
        }
        return u;
      });
      setUsers(updatedUsers?updatedUsers:null);
    })
  }

  const onSearch = async () => {
    const value = form.getFieldValue("search")
    if (!value || value.length < 3) return
    setLoading(true)
    const params = new URLSearchParams()
    params.append(searchType, form.getFieldValue("search"))
    console.log(ApiRoutes.USERS + "?" + params.toString())
    apiCall.get((ApiRoutes.USERS + "?" + params.toString()) as ApiRoutes.USERS).then((res) => {
        //FIXME: It's possible that request doesn't come in the same order as they're sent in. So it's possible that it would show the request of an old query
      console.log(res.data)
      setUsers(res.data)
      setLoading(false)
    })
  }

  return (
    <div style={{ padding: "3rem" }}>
      <Form
        form={form}
        name="search"
        onFinish={onSearch}
      >
     
            <Form.Item
              name="search"
              rules={[
                {
                  validator: (_, value) => {
                    if (searchType === "email") {
                      // Validate email
                      const emailRegex = /^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/
                      if (!emailRegex.test(value)) {
                        return Promise.reject(new Error(t("editRole.invalidEmail")))
                      }
                    }
                    // Validate name and surname
                    

                    return Promise.resolve()
                  },
                },
                  {
                    message: t("editRole.searchTooShort"),
                    min: 3,
                  }
              ]}
            >
              <Input
              size="large"
                addonBefore={
                  <Select
                    value={searchType}
                    onChange={(value) => setSearchType(value)}
                    style={{ width: 120 }}
                    options={[
                      { label: t("editRole.email"), value: "email" },
                      { label: t("editRole.name"), value: "name" },
                      { label: t("editRole.surname"), value: "surname" },
                    ]}
                  />
                }
              />
            </Form.Item>
      </Form>
      {users !== null ? (
        <>
          {loading ? (
            <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
              <Spin
                tip="Loading..."
                size="large"
            ><span> </span></Spin>
            </div>
          ) : (
            <UserList
              users={users}
              updateRole={updateRole}
            />
          )}
        </>
      ): <Typography.Text type="secondary">{t("editRole.searchTutorial")}</Typography.Text>}
    </div>
  )
}

export function EditRole() {
  return <ProfileContent />
}

export default EditRole
