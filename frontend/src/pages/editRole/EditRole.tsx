import { useEffect, useState } from "react"
import { Form, Input, Spin, Select, Typography } from "antd"
import UserList from "./components/UserList"
import { ApiRoutes, GET_Responses, UserRole } from "../../@types/requests.d"
import apiCall from "../../util/apiFetch"
import { useTranslation } from "react-i18next"
import { UsersListItem } from "./components/UserList"
import { useDebounceValue } from "usehooks-ts"

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

  const updateRole = (user: UsersListItem, role: UserRole) => {
    apiCall.patch(ApiRoutes.USER, { role: role }, { id: user.id }).then((res) => {
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
    apiCall.get((ApiRoutes.USERS + "?" + params.toString()) as ApiRoutes.USERS).then((res) => {

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
                    <Spin />
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
