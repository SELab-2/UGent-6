import { useContext, useEffect, useState } from "react"
import { Form, Input, Spin, Select, Typography, Space } from "antd"
import UserList from "./components/UserList"
import { ApiRoutes, GET_Responses, UserRole } from "../../@types/requests.d"
import apiCall from "../../util/apiFetch"
import { useTranslation } from "react-i18next"
import { UsersListItem } from "./components/UserList"
import { useDebounceValue } from "usehooks-ts"
import { UserContext } from "../../providers/UserProvider"
import useUser from "../../hooks/useUser"

export type UsersType = GET_Responses[ApiRoutes.USERS]
type SearchType = "name" | "email"
const ProfileContent = () => {
  const [users, setUsers] = useState<UsersType | null>(null)
  const myself = useUser()
  const [loading, setLoading] = useState(false)
  const [form] = Form.useForm()
  const firstSearchValue = Form.useWatch("first", form)
  const secondSearchValue = Form.useWatch("second", form)
  const searchValue = `${firstSearchValue || ''} ${secondSearchValue || ''}`.trim();
  const [debouncedSearchValue] = useDebounceValue(searchValue, 250)
  const [searchType, setSearchType] = useState<SearchType>("name")

  const { t } = useTranslation()

  const emailRegex = /^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/;

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
      if(user.id === myself.user?.id){
        myself.updateUser()
      }
    })
  }

  const [isError, setIsError] = useState(false)

  const checkValidate = () => {
    if (searchType === "email") {
      if (!emailRegex.test(form.getFieldValue("first"))) {
        return false
      } else {
        return true
      }
    } else {
      const firstValue = form.getFieldValue("first")
      const secondValue = form.getFieldValue("second")
      const firstValueLength = firstValue ? firstValue.length : 0
      const secondValueLength = secondValue ? secondValue.length : 0
      if (firstValueLength < 3 && secondValueLength < 3) {
        console.log("error")
        return false
      } else {
        console.log("no error")
        return true
      }
    }
  }

  const validate = () => {
    if (!checkValidate()) {
      setIsError(true)
    } else {
      setIsError(false)
    }
  }

  const onSearch = async () => {
    //validation
    if (!checkValidate()) {
      return
    }

    const firstValue = form.getFieldValue("first")
    setLoading(true)
    const params = new URLSearchParams()
    if (searchType === "email") {
      params.append(searchType, form.getFieldValue("first"))
    } else {
      const secondValue = form.getFieldValue("second")
      if (firstValue)  params.append("name", firstValue)
      if (secondValue) params.append("surname", secondValue)
    }
    console.log(params)
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
        onChange={validate}
        validateTrigger={[]}
      >
        <Form.Item>
          <Space.Compact style={{ display: 'flex' }}>
            <Select size="large"
                    value={searchType}
                    onChange={(value) => setSearchType(value)}
                    style={{ width: 120 }}
                    options={[
                      { label: t("editRole.email"), value: "email" },
                      { label: t("editRole.name"), value: "name" },
                    ]}
                />
            <Form.Item
              name="first"
              rules={[
                {
                  validator: (_, value) => {
                    // Validate email
                    if (searchType === "email") {
                      if (!emailRegex.test(value)) {
                        return Promise.reject(new Error(t("editRole.invalidEmail")));
                      }
                    // Validate name
                    } else if (searchType === "name") {
                      if (value && value.length < 3) {
                        return Promise.reject(new Error(t("editRole.nameError")));
                      }
                    }
                  },
                },
              ]}
              noStyle
            >
              <Input size="large" placeholder={searchType === "email" ? t("editRole.email") : t("editRole.name")}/>
            </Form.Item>
            {searchType === "name" && (
              <Form.Item
                name="second"
                rules={[
                  {
                    message: t("editRole.surnameError"),
                    min: 3,
                  },
                ]}
                noStyle
              >
                <Input size="large" placeholder={t("editRole.surname")}/>
              </Form.Item>
            )}
          </Space.Compact>
        </Form.Item>
        {isError && <Typography.Text type="danger">{searchType === "email" ? t("editRole.emailError") : t("editRole.nameError")}</Typography.Text>}
        <br />
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
