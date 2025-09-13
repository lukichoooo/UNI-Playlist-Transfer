// lukichoooo/music-playlist-converter/Music-Playlist-Converter-d0f2106ac07b1d284c8474589fcb1eed50f23c54/Frontend/PlaylistConverter/src/components/serviceSelectorMenu/PlaylistDetailsStep.tsx
import { useState } from "react";
import "./PlaylistDetailsStep.css";
// Make sure to import converterService if it's not already
import { converterService, type StreamingPlatform } from "../../services/ConverterService";

type PlaylistDetailsStepProps = {
    fromService: string | null;
    toService: string | null;
    // This prop should now be a function to update the parent's state
    onAuthenticationSuccess: () => void;
    authenticatedServices: string[];
    onBack: () => void;
};

export default function PlaylistDetailsStep({
    fromService,
    toService,
    authenticatedServices,
    onAuthenticationSuccess,
    onBack,
}: PlaylistDetailsStepProps)
{
    const [fromPlaylistId, setFromPlaylistId] = useState("");
    const [toPlaylistId, setToPlaylistId] = useState("");
    const [playlistName, setPlaylistName] = useState("");

    const isFromAuthenticated = fromService ? authenticatedServices.includes(fromService) : false;
    const isToAuthenticated = toService ? authenticatedServices.includes(toService) : false;

    const handleCreate = async () =>
    {
        if (!fromService || !toService || !isFromAuthenticated || !isToAuthenticated || !fromPlaylistId) return;

        try
        {
            await converterService.transferPlaylist(
                fromService as StreamingPlatform,
                toService as StreamingPlatform,
                fromPlaylistId,
                toPlaylistId, // This can be an empty string if not provided
                playlistName
            );
            alert("Playlist transferred successfully!");
        } catch (err)
        {
            console.error("Failed to transfer playlist:", err);
            alert("Failed to transfer playlist. Please check the console for more details.");
        }
    };

    const handleAuthenticate = async (platform: string) =>
    {
        if (!platform) return;
        try
        {
            await converterService.oauthLogin(platform as any);

            // TODO: implement redis and store tokens there

            // After successful authentication, notify the parent to refresh authenticated services
            onAuthenticationSuccess();
            console.log(`Successfully authenticated ${platform}`);
        } catch (err)
        {
            console.error(`Authentication failed for ${platform}:`, err);
        }
    };

    return (
        <div className="details-step">
            <h2>Playlist Details</h2>

            <div className="service-auth-container">
                {/* Left: FROM Service */}
                <div className="service-box">
                    <h3>From</h3>
                    <p><strong>{fromService || "Not selected"}</strong></p>
                    {fromService && (
                        <button
                            className={`auth-btn ${isFromAuthenticated ? "done" : ""}`}
                            onClick={() => handleAuthenticate(fromService)} // Use the new handler
                        >
                            {isFromAuthenticated ? "✔ Authenticated" : "Authenticate"}
                        </button>
                    )}
                </div>

                {/* Right: TO Service */}
                <div className="service-box">
                    <h3>To</h3>
                    <p><strong>{toService || "Not selected"}</strong></p>
                    {toService && (
                        <button
                            className={`auth-btn ${isToAuthenticated ? "done" : ""}`}
                            onClick={() => handleAuthenticate(toService)} // Use the new handler
                        >
                            {isToAuthenticated ? "✔ Authenticated" : "Authenticate"}
                        </button>
                    )}
                </div>
            </div>

            {/* Input */}
            <div className="input-group">
                <label>From Playlist ID</label>
                <input
                    type="text"
                    placeholder="Enter the source playlist ID"
                    value={fromPlaylistId}
                    onChange={(e) => setFromPlaylistId(e.target.value)}
                    required
                />
            </div>

            <div className="input-group">
                <label>To Playlist ID (Optional)</label>
                <input
                    type="text"
                    placeholder="Enter the destination playlist ID"
                    value={toPlaylistId}
                    onChange={(e) => setToPlaylistId(e.target.value)}
                />
            </div>

            <div className="input-group">
                <label>New Playlist Name (Optional)</label>
                <input
                    type="text"
                    placeholder="Enter new playlist name"
                    value={playlistName}
                    onChange={(e) => setPlaylistName(e.target.value)}
                />
            </div>

            {/* Buttons */}
            <div className="details-buttons">
                <button className="transfer-btn back-btn" onClick={onBack}>
                    ← Go Back
                </button>

                {isFromAuthenticated && isToAuthenticated && (
                    <button className="transfer-btn active" onClick={handleCreate}>
                        Create Playlist
                    </button>
                )}
            </div>
        </div>
    );
}