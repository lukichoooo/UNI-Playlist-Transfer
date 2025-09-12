import axios from "axios";
import { authService } from "./authService";
import { openOAuthPopup } from "./oauthHelper";

export type StreamingPlatform =
    | "SPOTIFY"
    | "YOUTUBE"
    | "SOUNDCLOUD"
    | "DEEZER"
    | "APPLEMUSIC"
    | "YOUTUBEMUSIC";

const BASE_URL = "http://localhost:8080/api/converter";
const PLATFORM_AUTH_URL = "http://localhost:8080/api/platformAuth/connect";


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
            // Convert platform to lowercase for URL path
            const platformPath = platform.toLowerCase();
            const authUrl = `${PLATFORM_AUTH_URL}/${platformPath}`; // Use the constant

            await openOAuthPopup(authUrl);
            // The promise will resolve when the popup closes after success
        } catch (err)
        {
            console.error(`OAuth login failed for ${platform}:`, err);
            // Re-throw the error so the calling component knows it failed
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

    authenticate = async (platform: StreamingPlatform): Promise<void> =>
    {
        try
        {
            await axios.post(`${BASE_URL}/auth`, platform, {
                headers: this.getAuthHeader(),
            });
        } catch (err)
        {
            console.error(`Failed to authenticate ${platform}:`, err);
        }
    };
}

export const converterService = new ConverterService();
