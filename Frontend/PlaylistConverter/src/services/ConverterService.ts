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
            const authUrl = `http://localhost:8080/api/platformAuth/connect/${platformPath}`;

            await openOAuthPopup(authUrl);
            // Backend now has the tokens associated with the logged-in user
        } catch (err)
        {
            console.error(`OAuth login failed for ${platform}:`, err);
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
