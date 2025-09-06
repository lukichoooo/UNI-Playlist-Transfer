// src/pages/OAuthSuccessPage.tsx
import { useEffect } from "react";

export default function OAuthSuccessPage()
{
    useEffect(() =>
    {
        const params = new URLSearchParams(window.location.search);
        const token = params.get("token");

        if (token)
        {
            // Send token to the opener window
            if (window.opener)
            {
                window.opener.postMessage({ token }, window.location.origin);
                window.close(); // close popup
            }
        }
    }, []);

    return <p>Logging you in...</p>;
}
