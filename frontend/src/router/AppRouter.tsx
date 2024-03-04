import { Routes, Route } from 'react-router-dom';
import Home from '../pages/index/Home';
import Dashboard from '../pages/dashboard/Dashboard';
import Profile from '../pages/profile/Profile';
import { AppRoutes } from '../@types/routes';



const AppRouter = () => {

  return (
   
    <Routes>
      <Route path={AppRoutes.HOME} element={<Home />} />
      <Route path={AppRoutes.DASHBOARD} element={<Dashboard />} />
      <Route path={AppRoutes.PROFILE} element={<Profile />} />
    </Routes>
  )

}

export default AppRouter;