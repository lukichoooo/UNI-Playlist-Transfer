// src/services/authService.ts
import api from "./api";
import { jwtDecode } from "jwt-decode";
import { authApi } from "./api";

import { openOAuthPopup } from "./oauthHelper";
const VITE_BASE_URL = import.meta.env.VITE_BASE_URL;


interface JwtPayload
{
    exp?: number;
    [key: string]: any;
}

class AuthService
{
    private TOKEN_KEY = "jwtToken";


    login = async (username: string, password: string): Promise<string> =>
    {
        const response = await authApi.post(`/login`, { username, password });
        const token = response.data.accessToken;
        this.saveToken(token);
        return token;
    };

    register = async (username: string, password: string): Promise<string> =>
    {
        const response = await authApi.post(`/register`, { username, password });
        const token = response.data.accessToken;
        this.saveToken(token);
        return token;
    };

    logout = () =>
    {
        this.removeToken();
    };


    // src/services/authService.ts (or a component where you trigger login)
    googleLogin = async (): Promise<void> =>
    {
        try
        {
            const token = await openOAuthPopup(`${VITE_BASE_URL}/oauth2/authorization/google`);
            this.saveToken(token);
            window.location.href = "/dashboard";
        } catch (err)
        {
            console.error("Google login failed:", err);
        }
    };

    githubLogin = async (): Promise<void> =>
    {
        try
        {
            const token = await openOAuthPopup(`${VITE_BASE_URL}/oauth2/authorization/github`);
            this.saveToken(token);
            window.location.href = "/dashboard";
        } catch (err)
        {
            console.error("GitHub login failed:", err);
        }
    };

    handleOAuthSuccess = (search: string): boolean =>
    {
        const params = new URLSearchParams(search);
        const token = params.get("token");
        if (!token) return false;

        this.saveToken(token);
        return true;
    };

    saveToken = (token: string) =>
    {
        localStorage.setItem(this.TOKEN_KEY, token);
    };

    removeToken = () =>
    {
        localStorage.removeItem(this.TOKEN_KEY);
    };


    getToken = async (): Promise<string | null> =>
    {
        let token = localStorage.getItem(this.TOKEN_KEY);
        if (!token)
        {
            console.log("No token found, fetching a new guest token...");
            token = await this.fetchGuestToken();
        }
        return token;
    };

    isTokenAvailable = async (): Promise<boolean> =>
    {
        const token = await this.getToken();

        if (!token)
        {
            return false;
        }

        try
        {
            const payload: JwtPayload = jwtDecode(token);
            const now = Math.floor(Date.now() / 1000);
            return payload.exp ? payload.exp > now : true;
        } catch (error)
        {
            console.error("Invalid token:", error);
            return false;
        }
    };

    private fetchGuestToken = async (): Promise<string | null> =>
    {
        try
        {
            const response = await authApi.post(`/guest`);

            // Add this validation check!
            const token = response.data?.accessToken;
            if (typeof token === 'string' && token.length > 0)
            {
                this.saveToken(token);
                return token;
            } else
            {
                console.error("Backend did not return a valid accessToken.", response.data);
                return null;
            }
        } catch (error)
        {
            console.error("Failed to fetch guest token:", error);
            return null;
        }
    };

    isLoggedIn = (): boolean =>
    {
        const token = localStorage.getItem(this.TOKEN_KEY);
        if (!token) return false;

        try
        {
            const payload: JwtPayload = jwtDecode(token);
            const now = Math.floor(Date.now() / 1000);

            if (payload.exp && payload.exp < now)
            {
                return false; // Token is expired
            }

            // Return true ONLY if the user is not a guest.
            return payload.auth?.includes("ROLE_ANONYMOUS") === false;
        } catch (error)
        {
            return false;
        }
    };

}

export const authService = new AuthService();
