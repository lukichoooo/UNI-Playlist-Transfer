import { Route, Routes } from 'react-router-dom'
import HomePage from './pages/public/HomePage'
import AboutPage from './pages/public/AboutPage'
import Dashboard from './pages/private/Dashboard'
import Settings from './pages/private/Settings'
import Profile from './pages/private/Profile'
import NavBarWrapper from './layouts/NavBarWrapper'
import PrivatePageWrapper from './layouts/PrivatePageWrapper'
import LoginPage from './pages/login/LoginPage'

export default function App()
{
  return (
    <Routes>
      {/* Login
       */}
      <Route path="/login" element={<LoginPage />} />

      {/* public */}
      <Route element={<NavBarWrapper />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />

        {/* private */}
        <Route element={<PrivatePageWrapper />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="/profile" element={<Profile />} />
        </Route>

        {/* Page not found 404 fallback */}
        <Route
          path="*"
          element={
            <div className="container">
              <h2>Not found</h2>
            </div>
          }
        />
      </Route>
    </Routes>
  )
}
