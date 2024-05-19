import { useEffect, useRef, useState } from "react"
import apiCall from "../../util/apiFetch"
import { Button, Input, InputRef, Result, Select, Space, Typography } from "antd"

const { Option } = Select

const ApiTest = () => {
  const [result, setResult] = useState<string | null>(null)
  const [method, setMethod] = useState<string>("get")
  const routeRef = useRef<InputRef>(null)
  const [error, setError] = useState<[string, number] | null>(null)
  const [requestBody, setRequestBody] = useState<string>("{}")


  const makeApiCall = async () => {
    const route = routeRef.current?.input?.value
    if (!route) return

    setError(null)
    setResult(null)
    try {
      const body = requestBody

      if (method !== "get" && body) {
        //@ts-ignore
        const response = await apiCall[method](route, JSON.parse(body))
        console.log(response)
        setResult(JSON.stringify(response.data, null, 2))
        return
      }
      //@ts-ignore
      const response = await apiCall[method](route)
      console.log(response)
      setResult(JSON.stringify(response.data, null, 2))
    } catch (err: any) {
      console.log(err)
      setError([err.message + ".\n " + (err?.response?.data?.message ?? err.response?.data ?? ""), err.status])
    }
  }

  const selectBefore = (
    <Select
      defaultValue="get"
      onChange={(value) => setMethod(value)}
      style={{ width: "100px" }}
    >
      <Option value="get">GET</Option>
      <Option value="post">POST</Option>
      <Option value="put">PUT</Option>
      <Option value="delete">DELETE</Option>
      <Option value="patch">PATCH</Option>
    </Select>
  )

  return (
    <div style={{ display: "flex", justifyContent: "center", margin: "0 3rem", height: "100%" }}>
      <div style={{ width: "700px", height: "100%" }}>
        <Typography.Title level={4}>Headers:</Typography.Title>
        <Typography.Text
          code
          style={{ maxHeight: "100%" }}
        >
          {JSON.stringify(
                {
                  "Content-Type": "application/json",
                },
                null,
                2
              )}
        </Typography.Text>

        <Typography.Title level={4}>Test:</Typography.Title>

        <Space.Compact style={{ width: "100%" }}>
          <Input
            ref={routeRef}
            size="large"
            addonBefore={selectBefore}
            defaultValue="/web/api/test"
          />
          <Button
            size="large"
            onClick={makeApiCall}
            type="primary"
          >
            Send
          </Button>
        </Space.Compact>

        {method !== "get" && (
          <Input.TextArea
            onChange={(e) => setRequestBody(e.currentTarget.value)}
            value={requestBody}
            placeholder="body"
            style={{ marginTop: "1rem" }}
            rows={7}
          />
        )}

        <Typography.Title level={4}>Result:</Typography.Title>

        {error ? (
          <Result
            status="error"
            title={"Request failed: " + (error[1] ?? "")}
          >
            <Typography.Text code>{error[0]}</Typography.Text>
          </Result>
        ) : (
          <pre>{result}</pre>
        )}
        <br />
        <br />
        <br />
      </div>
    </div>
  )
}

export default ApiTest
