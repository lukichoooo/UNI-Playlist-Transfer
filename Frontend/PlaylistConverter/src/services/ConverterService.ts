// src/services/ConverterService.ts
import api from "./api";
import { authService } from "./authService";
import { openOAuthPopup } from "./oauthHelper";
import type { PlaylistSearchDto } from "../types";
const VITE_BASE_URL = import.meta.env.VITE_BASE_URL;

export type StreamingPlatform =
    | "SPOTIFY"
    | "YOUTUBE"
    | "SOUNDCLOUD"
    | "DEEZER"
    | "APPLEMUSIC"
    | "YOUTUBEMUSIC";

const BASE_URL = `${VITE_BASE_URL}/api/converter`;
const PLATFORM_AUTH_URL = `${VITE_BASE_URL}/api/platformAuth/connect`;


class ConverterService
{
    private getAuthHeader = async () =>
    {
        const token = await authService.getToken();
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    oauthLogin = async (platform: StreamingPlatform): Promise<void> =>
    {
        try
        {
            const platformPath = platform.toLowerCase();

            let authUrl = `${PLATFORM_AUTH_URL}/${platformPath}`;

            const token = await authService.getToken();
            if (token)
            {
                authUrl += `?jwt_token=${token}`;
            }
            await openOAuthPopup(authUrl);

        } catch (err)
        {
            console.error(`OAuth login failed for ${platform}:`, err);
            throw err;
        }
    };


    getAuthenticatedServices = async (): Promise<StreamingPlatform[]> =>
    {
        try
        {
            const response = await api.get(`${BASE_URL}/authenticatedPlatforms`, {
                headers: await this.getAuthHeader(),
            });
            return response.data;
        } catch (err)
        {
            console.error("Failed to fetch authenticated services:", err);
            return [];
        }
    };

    getPlaylists = async (platform: StreamingPlatform): Promise<PlaylistSearchDto[]> =>
    {
        try
        {
            const response = await api.get(`${BASE_URL}/playlists`, {
                headers: await this.getAuthHeader(),
                params: { platform },
            });
            return response.data;
        } catch (err)
        {
            console.error("Failed to fetch playlists:", err);
            return [];
        }
    }

    getTransferState = async (): Promise<string> =>
    {
        try
        {
            const response = await api.get(`${BASE_URL}/transferState`, {
                headers: await this.getAuthHeader(),
            });
            return response.data;
        } catch (err)
        {
            console.error("Failed to fetch transfer state:", err);
            return "IDLE";
        }
    }


    transferPlaylist = async (
        transferState: string,
        fromPlatform: StreamingPlatform,
        toPlatform: StreamingPlatform,
        fromPlaylistId: string,
        newPlaylistName: string
    ): Promise<void> => 
    {
        try
        {
            await api.post(`${BASE_URL}/convert`, {
                transferState,
                fromPlatform,
                toPlatform,
                fromPlaylistId,
                newPlaylistName
            }, {
                headers: await this.getAuthHeader(),
            });
        } catch (err)
        {
            console.error("Failed to transfer playlist:", err);
            throw err;
        }
    };
}

export const converterService = new ConverterService();