// src/services/ConverterService.ts
import axios from "axios";
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
    private getAuthHeader()
    {
        const token = authService.getToken();
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    oauthLogin = async (platform: StreamingPlatform): Promise<void> =>
    {
        try
        {
            const platformPath = platform.toLowerCase();

            let authUrl = `${PLATFORM_AUTH_URL}/${platformPath}`;

            // If the user is logged in, append their JWT to the URL.
            if (authService.isLoggedIn())
            {
                const token = authService.getToken();
                if (token)
                {
                    authUrl += `?jwt_token=${token}`;
                }
                await openOAuthPopup(authUrl);
            }
            else
            {
                // TODO: Handle the case where the user is not logged in.
                throw new Error("User must be logged in to authenticate with a platform.");
            }

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
            const response = await axios.get(`${BASE_URL}/authenticatedPlatforms`, {
                headers: this.getAuthHeader(),
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
            const response = await axios.get(`${BASE_URL}/playlists`, {
                headers: this.getAuthHeader(),
                params: { platform },
            });
            return response.data;
        } catch (err)
        {
            console.error("Failed to fetch playlists:", err);
            return [];
        }
    }

    transferPlaylist = async (
        fromPlatform: StreamingPlatform,
        toPlatform: StreamingPlatform,
        fromPlaylistId: string,
        newPlaylistName: string
    ): Promise<void> =>
    {
        try
        {
            await axios.post(`${BASE_URL}/convert`, {
                fromPlatform,
                toPlatform,
                fromPlaylistId,
                newPlaylistName
            }, {
                headers: this.getAuthHeader(),
            });
        } catch (err)
        {
            console.error("Failed to transfer playlist:", err);
            throw err;
        }
    };
}

export const converterService = new ConverterService();