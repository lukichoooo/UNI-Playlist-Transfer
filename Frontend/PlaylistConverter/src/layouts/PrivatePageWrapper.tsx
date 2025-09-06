import type { JSX } from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";

export default function PrivatePageWrapper(): JSX.Element
{
    const location = useLocation();
    const token = localStorage.getItem("token"); // your JWT

    // if no token, redirect to login
    if (!token)
    {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // user is authenticated, render nested routes
    return <Outlet />;
}
