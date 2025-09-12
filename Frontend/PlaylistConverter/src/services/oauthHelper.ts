// src/services/oauthHelper.ts
export function openOAuthPopup(url: string, width = 500, height = 600): Promise<string>
{
    return new Promise((resolve, reject) =>
    {
        const left = window.screenX + (window.innerWidth - width) / 2;
        const top = window.screenY + (window.innerHeight - height) / 2;

        const popup = window.open(
            url,
            "_blank",
            `width=${width},height=${height},top=${top},left=${left}`
        );

        if (!popup) return reject(new Error("Popup blocked"));

        const listener = (event: MessageEvent) =>
        {
            if (event.origin !== window.location.origin) return; // security

            // This handles JWT login
            if (event.data?.token)
            {
                window.removeEventListener("message", listener);
                popup.close();
                resolve(event.data.token);
            }

            // This handles platform auth success
            if (event.data?.type === "auth-success")
            {
                window.removeEventListener("message", listener);
                popup.close();
                resolve("success"); // Resolve with a success message
            }
        };

        window.addEventListener("message", listener);

        // Optional: reject if user closes popup without logging in
        const interval = setInterval(() =>
        {
            if (popup.closed)
            {
                clearInterval(interval);
                window.removeEventListener("message", listener);
                reject(new Error("Popup closed by user"));
            }
        }, 500);
    });
}
