import React, { useState } from "react";
import "./PlaylistDetailsStep.css";
import { playlistService } from "../../services/PlaylistService";

type PlaylistDetailsStepProps = {
    fromService: string | null;
    toService: string | null;
    authenticatedServices: string[];
    onAuthenticate: (serviceId: string) => void;
    onBack: () => void;
};

export default function PlaylistDetailsStep({
    fromService,
    toService,
    authenticatedServices,
    onAuthenticate,
    onBack,
}: PlaylistDetailsStepProps)
{
    const [playlistName, setPlaylistName] = useState("");

    const isFromAuthenticated = fromService ? authenticatedServices.includes(fromService) : false;
    const isToAuthenticated = toService ? authenticatedServices.includes(toService) : false;

    const handleCreate = async () =>
    {
        if (!fromService || !toService || !isFromAuthenticated || !isToAuthenticated) return;

        try
        {
            const data = await playlistService.transfer(
                // TODO: replace with real values
            );
            console.log("Playlist created:", data);
            alert("✅ Playlist created successfully!");
        } catch (err)
        {
            console.error(err);
            alert("❌ Failed to create playlist");
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
                            onClick={() => onAuthenticate(fromService)}
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
                            onClick={() => onAuthenticate(toService)}
                        >
                            {isToAuthenticated ? "✔ Authenticated" : "Authenticate"}
                        </button>
                    )}
                </div>
            </div>

            {/* Input */}
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
