// src/services/api.ts
import axios from "axios";
import { authService } from "./authService";

const VITE_BASE_URL = import.meta.env.VITE_BASE_URL;

// 1. Create a "raw" instance for authentication calls. NO interceptor here.
export const authApi = axios.create({
    baseURL: `${VITE_BASE_URL}/api/auth`, // Point it directly at the auth path
});

// 2. Create the general instance for all other API calls.
const api = axios.create({
    baseURL: VITE_BASE_URL, // Use the root base URL
});

// 3. Attach the interceptor ONLY to the general 'api' instance.
api.interceptors.request.use(async (config) =>
{
    const token = await authService.getToken();
    if (token)
    {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;