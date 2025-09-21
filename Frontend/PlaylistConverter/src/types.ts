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
    token: string;
}

export type PlaylistSearchDto =
    {
        id: string;
        name: string;
        totalTracks: number;
    }

export type StreamingPlatform =
    | "SPOTIFY"
    | "YOUTUBE"
    | "SOUNDCLOUD"
    | "DEEZER"
    | "APPLEMUSIC"
    | "YOUTUBEMUSIC";

export interface JwtPayload
{
    sub: string;
    auth: string[];
    exp: number;
    id: number;
    iat: number;
}
