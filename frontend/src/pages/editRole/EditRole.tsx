import { useEffect, useState, useRef } from "react";
import { Row, Col, Form, Input, Button, Spin, Select } from "antd";
import UserList from "./components/UserList"
import { ApiRoutes, GET_Responses, UserRole } from "../../@types/requests.d";
import apiCall from "../../util/apiFetch";
import { useTranslation } from "react-i18next";
import { UsersListItem } from "./components/UserList";

export type UsersType = GET_Responses[ApiRoutes.USERS]

const ProfileContent = () => {
    const [users, setUsers] = useState<UsersType | null>(null);
    const [searchField, setSearchField] = useState<string>("email");
    const searchFieldRef = useRef<string>("email");
    const [searched, setSearched] = useState(false);
    const [form] = Form.useForm();
    const { t } = useTranslation();

    function updateRole(user: UsersListItem, role: UserRole) {
      //here user is of type User (not UsersListItem), but it seems to work because the needed properties are named the same
      console.log(user)
      apiCall.patch(ApiRoutes.USER, {role: role}, {id: user.id}).then((res) => {
        console.log(res.data);
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

    const onSearch = (values: any) => {
      setSearched(true);
      setUsers(null);
      //search operation here
      const params = Object.entries(values)
        .filter(([key, value]) => value !== undefined)
        .reduce((obj, [key, value]) => ({ ...obj, [key]: value }), {});
      const queryString = Object.entries(params)
        .map(([key, value]) => `${key}=${value}`)
        .join('&');
      console.log(queryString);
      apiCall.get(ApiRoutes.USERS,{params:queryString}).then((res) => {
        console.log(res.data)
        setUsers(res.data);
      })
      
    };

return (
  <div style={{padding: "3rem"}}>
      <Form
          form={form}
          name="search"
          onFinish={onSearch}
      >
          <Row gutter={24} justify="space-between">
              {/*<Col span={7}>
                  <Form.Item
                      name="email"
                      rules={[
                          { type: 'email', message: t("editRole.emailError") },
                      ]}
                  >
                      <Input placeholder={t("editRole.email")} />
                  </Form.Item>
              </Col>

              <Col span={7}>
                  <Form.Item
                      name="name"
                      rules={[
                          { min: 3, message: t("editRole.nameError") },
                      ]}
                  >
                      <Input placeholder={t("editRole.name")} />
                  </Form.Item>
              </Col>

              <Col span={7}>
                  <Form.Item
                      name="surname"
                      rules={[
                          { min: 3, message: t("editRole.surnameError") },
                      ]}
                  >
                      <Input placeholder={t("editRole.surname")} />
                  </Form.Item>
                    </Col>*/}
            <Col span={20}>
                <Form.Item
                name = "search"
                rules={[
                    {
                        validator: (_, value) => {
                            if (searchFieldRef.current === 'email') {
                                // Validate email
                                const emailRegex = /^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/;
                                if (!emailRegex.test(value)) {
                                    return Promise.reject(new Error(t("editRole.invalidEmail")));
                                }
                            } else {
                                // Validate name and surname
                                if (value.length < 3) {
                                    return Promise.reject(new Error(t("editRole.shortValue")));
                                }
                            }
                            return Promise.resolve();
                        },
                    },
                ]}>
                    <Input 
                        addonBefore={
                            <Select defaultValue="email" onChange={(value) => {console.log(value); searchFieldRef.current = value}} style={{ width: 120 }} options={[
                                { label: t("editRole.email"), value: "email" },
                                { label: t("editRole.name"), value: "name" },
                                { label: t("editRole.surname"), value: "surname" },
                            ]}/>
                        }
                        placeholder={t(`editRole.${searchFieldRef.current}`)} 
                    />
                </Form.Item>
            </Col>

              <Col span={4}>
                  <Form.Item shouldUpdate>
                      {() => (
                          <Button
                              type="primary"
                              htmlType="submit"
                              disabled={
                                ['name', 'surname', 'email'].every(field => !form.getFieldValue(field)) ||
                                form.getFieldsError().filter(({ errors }) => errors.length).length > 0
                              }
                              style={{ width: '100%' }}
                          >
                              {t("editRole.search")}
                          </Button>
                      )}
                  </Form.Item>
              </Col>
          </Row>
      </Form>
      {searched ? (
          users === null ? (
              <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
                  <Spin
                      tip="Loading..."
                      size="large"
                  />
              </div>
          ) : (
              <UserList users={users} updateRole={updateRole} />
          )
      ) : (
        <div style={{ textAlign: 'center', marginTop: '2rem' }}>
          <p>{t("editRole.searchTutorial")}</p>
        </div>
      )}
      </div>
  );
};

export function EditRole() {
    return (
        <ProfileContent />
      )
};

export default EditRole;