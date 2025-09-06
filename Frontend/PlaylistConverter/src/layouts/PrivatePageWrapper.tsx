import type { JSX } from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { authService } from "../services/authService"; // adjust path

export default function PrivatePageWrapper(): JSX.Element
{
    const location = useLocation();

    if (!authService.isLoggedIn())
    {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return <Outlet />;
}
