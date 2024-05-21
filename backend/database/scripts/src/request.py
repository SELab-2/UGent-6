import requests
import json

class Requester:
    token: str

    def __init__(self, token):
        token = token

    def make_request(self, method, url, data):

        headers = {'Authorization': 'Bearer ' + self.token}

        response = requests.request(method, url, data=json.dumps(data), headers=headers)
        if response.status_code == 200:
            print('Data inserted successfully to: ' + method + ' ' + url)
        else:
            print(str(response.status_code)+ ' ' + 'Failed to insert data to: ' + method + ' ' + url)

    def post(self, url, data):
        self.make_request(method='POST', url=url, data=data)