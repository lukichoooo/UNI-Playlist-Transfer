import React, { useState } from "react";
import "./PlaylistDetailsStep.css";

type PlaylistDetailsStepProps = {
    fromService: string | null;
    toService: string | null;
    authenticatedServices: string[];
    onAuthenticate: (serviceId: string) => void; // callback for authentication
    onBack: () => void; // callback to go back a step
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
    const [sourceLink, setSourceLink] = useState("");

    const isFromAuthenticated = fromService ? authenticatedServices.includes(fromService) : false;
    const isToAuthenticated = toService ? authenticatedServices.includes(toService) : false;

    const handleCreate = () =>
    {
        if (!fromService || !toService) return;
        console.log({ fromService, toService, playlistName, sourceLink });
    };

    return (
        <div className="details-step">
            <h2>Playlist Details</h2>

            <div className="selected-services">
                <p>
                    From: <strong>{fromService || "Not selected"}</strong>
                    {fromService && !isFromAuthenticated && (
                        <span
                            className="auth-warning"
                            onClick={() => onAuthenticate(fromService)}
                            style={{ cursor: "pointer" }}
                        >
                            {" "} - Authenticate if needed
                        </span>
                    )}
                </p>

                <p>
                    To: <strong>{toService || "Not selected"}</strong>
                    {toService && !isToAuthenticated && (
                        <span
                            className="auth-warning"
                            onClick={() => onAuthenticate(toService)}
                            style={{ cursor: "pointer" }}
                        >
                            {" "} - Click here to authenticate
                        </span>
                    )}
                </p>
            </div>

            <div className="input-group">
                <label>Source Playlist Link / Name</label>
                <input
                    type="text"
                    placeholder="Enter playlist link or name"
                    value={sourceLink}
                    onChange={(e) => setSourceLink(e.target.value)}
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

            <div className="details-buttons">
                <button className="transfer-btn back-btn" onClick={onBack}>
                    ← Go Back
                </button>

                <button
                    className={`transfer-btn ${fromService && toService && isToAuthenticated ? "active" : "disabled"}`}
                    onClick={handleCreate}
                    disabled={!(fromService && toService && isToAuthenticated)}
                >
                    {fromService && toService && isToAuthenticated
                        ? "Create Playlist"
                        : "Complete all steps"}
                </button>
            </div>
        </div>
    );
}
