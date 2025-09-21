import { Route, Routes } from 'react-router-dom'
import Footer from './components/Footer/Footer'
import NavBarWrapper from './layouts/NavBarWrapper'
import PrivatePageWrapper from './layouts/PrivatePageWrapper'
import LoginPage from './pages/Auth/LoginPage'
import OAuthSuccessPage from './pages/Auth/OAuthSuccessPage'
import RegisterPage from './pages/Auth/RegisterPage'
import Dashboard from './pages/private/Dashboard'
import Profile from './pages/private/Profile'
import Settings from './pages/private/Settings'
import AboutPage from './pages/public/AboutPage'
import HomePage from './pages/public/HomePage'
import PlatformAuthSuccessPage from './pages/Auth/PlatformAuthSuccessPage'

export default function App()
{
  return (
    <div className="app-container">
      <Routes>
        {/* Login */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth-success" element={<OAuthSuccessPage />} />
        <Route path="/platform-auth-success" element={<PlatformAuthSuccessPage />} />

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
                <p>ERROR 404</p>
                <p>Page Not found</p>
              </div>
            }
          />
        </Route>
      </Routes>
      <Footer />
    </div>
  )
}
