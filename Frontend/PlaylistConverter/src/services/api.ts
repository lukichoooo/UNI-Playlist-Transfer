// src/api.ts
import axios from "axios";
const VITE_BASE_URL = import.meta.env.VITE_BASE_URL;

const BASE_URL = `${VITE_BASE_URL}/api/auth`;

// Axios instance
const api = axios.create({
    baseURL: BASE_URL,
});

// Attach JWT from localStorage to every request
api.interceptors.request.use((config) =>
{
    const token = localStorage.getItem("jwtToken");
    if (token)
    {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;
