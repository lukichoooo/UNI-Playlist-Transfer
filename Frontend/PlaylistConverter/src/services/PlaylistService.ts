// src/services/playlistService.ts
import axios from "axios";

type MusicService = "SPOTIFY" | "YOUTUBE" | "SOUNDCLOUD" | "DEEZER" | "APPLE_MUSIC";

type OAuthTokenResponse
{
    accessToken: string;
    service: MusicService;
}

const BASE_URL = "http://localhost:8080/api/playlist";


export interface AuthPayload
{
    service: MusicService;
}

export interface CreatePlaylistPayload
{
    service: MusicService;
    playlistName: string;
    tracks: string[]; // array of track IDs or URIs
    accessToken: string;
}

export interface GetPlaylistsPayload
{
    service: MusicService;
    accessToken: string;
}

export interface PlaylistTransferPayload
{
    fromService: MusicService;
    toService: MusicService;
    playlistName: string;
    sourceLink: string;
    fromAccessToken: string;
    toAccessToken: string;
}

export class PlaylistService
{
    // Trigger OAuth2 login for a given service
    async authenticate(service: MusicService)
    {
        const { data } = await axios.post(`${BASE_URL}/auth`, { service });
        return data; // backend handles OAuth redirect or token return
    }

    // Create a playlist on the target service
    async createPlaylist(payload: CreatePlaylistPayload)
    {
        const { data } = await axios.post(`${BASE_URL}/create`, payload, {
            headers: { "Content-Type": "application/json" },
        });
        return data; // backend returns created playlist info
    }

    // Get playlists from a given service for the user
    async getPlaylists(payload: GetPlaylistsPayload)
    {
        const { data } = await axios.post(`${BASE_URL}/list`, payload, {
            headers: { "Content-Type": "application/json" },
        });
        return data; // backend returns list of playlists
    }
}

export const playlistService = new PlaylistService();
