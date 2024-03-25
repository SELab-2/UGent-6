import { Routes, Route } from 'react-router-dom';
import Home from '../pages/index/Home';
import Dashboard from '../pages/dashboard/Dashboard';
import Profile from '../pages/profile/Profile';
import { AppRoutes } from '../@types/routes';
import ApiTest from '../pages/apiTest/ApiTest';
import Error from "../pages/error/Error";
import {useTranslation} from "react-i18next";



const AppRouter = () => {
    const { t } = useTranslation()

  return (

      <Routes>
          <Route path={AppRoutes.HOME} element={<Home />} />
          <Route path={AppRoutes.DASHBOARD} element={<Dashboard />} />
          <Route path={AppRoutes.PROFILE} element={<Profile />} />
          <Route path="/api-test" element={<ApiTest/>} />
          <Route path={AppRoutes.ERROR} element={<Error/>} />

          {/*De 404 errorpagina route, deze moet de laatste lijn staan, routes hieronder worden nooit gematchd!*/}
          <Route path={"*"} element={<Error errorCode={404} errorMessage={t("error.404_message")}/>} />
      </Routes>
  )

}

export default AppRouter;