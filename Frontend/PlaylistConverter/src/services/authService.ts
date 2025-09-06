// src/services/authService.ts
import axios from "axios";
import { jwtDecode } from "jwt-decode";

import { openOAuthPopup } from "./oauthHelper";


interface JwtPayload
{
    exp?: number;
    [key: string]: any;
}

const BASE_URL = "http://localhost:8080/api/auth";

class AuthService
{
    private TOKEN_KEY = "jwtToken";

    login = async (username: string, password: string): Promise<string> =>
    {
        const response = await axios.post(`${BASE_URL}/login`, { username, password });
        const token = response.data.token;
        this.saveToken(token);
        return token;
    };

    register = async (username: string, password: string): Promise<string> =>
    {
        const response = await axios.post(`${BASE_URL}/register`, { username, password });
        const token = response.data.token;
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
            const token = await openOAuthPopup("http://localhost:8080/oauth2/authorization/google");
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
            const token = await openOAuthPopup("http://localhost:8080/oauth2/authorization/github");
            this.saveToken(token);
            window.location.href = "/dashboard";
        } catch (err)
        {
            console.error("GitHub login failed:", err);
        }
    };




    // NEW: parse URL search params and save token
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

    getToken = (): string | null =>
    {
        return localStorage.getItem(this.TOKEN_KEY);
    };

    removeToken = () =>
    {
        localStorage.removeItem(this.TOKEN_KEY);
    };

    isLoggedIn = (): boolean =>
    {
        const token = this.getToken();
        if (!token) return false;

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
}

export const authService = new AuthService();
