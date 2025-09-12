// src/pages/Auth/PlatformAuthSuccessPage.tsx
import { useEffect } from "react";

export default function PlatformAuthSuccessPage()
{
    useEffect(() =>
    {
        // Notify the window that opened the popup
        if (window.opener)
        {
            window.opener.postMessage({ type: "auth-success" }, window.location.origin);
        }
        // Close the popup window
        window.close();
    }, []);

    return <p>Authentication successful! You can close this window.</p>;
}