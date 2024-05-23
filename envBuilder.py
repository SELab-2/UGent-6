

class envBuilder():
  def __init__(self):
    self.env = {}
    self.javaEnv = {'client-secret': ['azure.activedirectory.b2c.client-secret'],'client-id':['azure.activedirectory.client-id'],'tenant-id':['azure.activedirectory.tenant-id'],'PGP':['spring.datasource.password'],'PGU':['spring.datasource.username']}
    self.expressEnv = {'URI':['REDIRECT_URI','FRONTEND_URI','BACKEND_API_ENDPOINT'],
                       'client-id':['CLIENT_ID'],'client-secret':['CLIENT_SECRET'],
                       'tenant-id':['TENANT_ID'],'PGP':['DB_PASSWORD'],'PGU':['DB_USER'],'DB_HOST':['DB_HOST'],
                       'DB_PORT':['DB_PORT'],'DB_NAME':['DB_NAME'],'EXPRESS_SESSION_SECRET':['EXPRESS_SESSION_SECRET']}
    self.javaEnvLocation = 'backend/app/src/main/resources/application-secrets.properties'
    self.expressEnvLocation = 'backend/web-bff/App/.env'
  def readEnv(self):
    with open('.env', 'r') as file:
      for line in file:
        [key, value] = line.split('=')
        self.env[key] = value

  def javaBuilder(self):
    with open(self.javaEnvLocation, 'a+') as file:
      for  key in self.javaEnv:
        if key in self.env:
          value = self.env[key]
          if value == '':
              print(f'{key} is empty')
          else:
            for envName in self.javaEnv[key]:
              file.seek(0)  
              if sum(line.count(f'{envName}') for line in file) == 0:
                file.write(f'{envName}={value}\n')
        else :
          print(f'{key} not found in .env file')

  def expressBuilder(self):
    with open(self.expressEnvLocation, 'a+') as file:
      for  key in self.expressEnv:
        if key in self.env:
          value = self.env[key]
          if value == '':
              print(f'{key} is empty')
          else:
            for envName in self.expressEnv[key]:
              file.seek(0)  
              if sum(line.count(f'{envName}') for line in file) == 0:
                file.write(f'{envName}={value}\n')
        else :
          print(f'{key} not found in .env file')


if __name__ == '__main__':
  env = envBuilder()
  env.readEnv()
  env.javaBuilder()
  env.expressBuilder()
