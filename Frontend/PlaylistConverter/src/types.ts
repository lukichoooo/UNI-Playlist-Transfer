export type UserPublicDto = {
    id: number;
    username: string;
}

export type UserPrivateDto = {
    id: number;
    username: string;
    email: string;
    password: string;
}

export type LoginRequest = {
    username: string;
    password: string;
}

export type RegisterRequest = {
    username: string;
    password: string;
}

export type AuthResponse = {
    token: string; // JWT returned from backend
}
