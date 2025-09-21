// lukichoooo/music-playlist-converter/Music-Playlist-Converter-d0f2106ac07b1d284c8474589fcb1eed50f23c54/Frontend/PlaylistConverter/src/components/serviceSelectorMenu/PlaylistDetailsStep.tsx
import { useEffect, useState } from "react";
import "./PlaylistDetailsStep.css";
import { converterService, type StreamingPlatform } from "../../services/ConverterService";
import { PlaylistDropdown } from "./PlaylistDropdown";
import type { PlaylistSearchDto } from "../../types";
import TransferProgress from "../Progress Bar/TransferProgress";


type PlaylistDetailsStepProps = {
    fromService: string | null;
    toService: string | null;
    // This prop should now be a function to update the parent's state
    refreshAuthenticatedServices: () => void;
    authenticatedServices: string[];
    onBack: () => void;
};

export default function PlaylistDetailsStep({
    fromService,
    toService,
    authenticatedServices,
    refreshAuthenticatedServices: onAuthenticationSuccess,
    onBack,
}: PlaylistDetailsStepProps)
{
    const [selectedPlaylist, setSelectedPlaylist] = useState<PlaylistSearchDto | null>(null as any);
    const [playlistModalOpen, setPlaylistModalOpen] = useState(false);
    const [sortedFromPlaylists, setSortedFromPlaylists] = useState<PlaylistSearchDto[]>([]); // Placeholder data
    // TODO: fetch playlists based on fromService when it changes

    const [playlistName, setPlaylistName] = useState("");

    const isFromAuthenticated = fromService ? authenticatedServices.includes(fromService) : false;
    const isToAuthenticated = toService ? authenticatedServices.includes(toService) : false;
    const [isTransferring, setIsTransferring] = useState(false);

    const [transferState, setTransferState] = useState<string>("IDLE");
    const [isLoadingPlaylists, setIsLoadingPlaylists] = useState(false);


    useEffect(() =>
    {
        const fetchPlaylists = async () =>
        {
            if (fromService && isFromAuthenticated)
            {
                setIsLoadingPlaylists(true);
                try
                {
                    const playlists = await converterService.getPlaylists(fromService as StreamingPlatform);
                    setSortedFromPlaylists(playlists);
                } catch (error)
                {
                    console.error('Failed to fetch playlists:', error);
                    // Handle error state if needed
                } finally
                {
                    setIsLoadingPlaylists(false);
                }
            }
        };
        fetchPlaylists();
    }, [fromService, isFromAuthenticated]);


    const handleCreate = async () =>
    {
        if (!fromService || !toService || !isFromAuthenticated || !isToAuthenticated || !selectedPlaylist) return;

        const transferState = await converterService.getTransferState();
        if (!transferState) return;

        setTransferState(transferState);
        setIsTransferring(true);

        // Fire-and-forget: don't await
        converterService.transferPlaylist(
            transferState,
            fromService as StreamingPlatform,
            toService as StreamingPlatform,
            selectedPlaylist.id,
            playlistName
        ).catch(err =>
        {
            console.error("Transfer failed:", err);
            alert("Playlist transfer failed!");
        });
    };

    const handleTransferComplete = () =>
    {
        console.log("Transfer finished, closing overlay.");
        setIsTransferring(false);
        setTransferState("IDLE");
    };


    const handleAuthenticate = async (platform: string) =>
    {
        if (!platform) return;

        await converterService.oauthLogin(platform as any);

        // TODO: implement redis and store tokens there

        // After successful authentication, notify the parent to refresh authenticated services
        onAuthenticationSuccess();

        console.log(`Successfully authenticated ${platform}`);

        const playlists = await converterService.getPlaylists(fromService as StreamingPlatform);
        setSortedFromPlaylists(playlists);
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

            {/* Display selected playlist above the button */}
            {selectedPlaylist && (
                <p className="selected-playlist">
                    Selected Playlist: <strong>{selectedPlaylist?.name}</strong> <br />
                    <small>Total Tracks: {selectedPlaylist?.totalTracks}</small>
                </p>
            )}
            {isFromAuthenticated &&
                <button className="choose-playlist-btn" onClick={() => setPlaylistModalOpen(true)}>Choose Playlist</button>
            }
            {/* Input */}
            <div className="input-group">
                <PlaylistDropdown
                    fromPlaylists={sortedFromPlaylists}
                    selectedPlaylist={selectedPlaylist}
                    setSelectedPlaylist={setSelectedPlaylist}
                    isOpen={playlistModalOpen}
                    onClose={() => setPlaylistModalOpen(false)}
                    isLoading={isLoadingPlaylists}
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

            <TransferProgress
                show={isTransferring}
                transferState={transferState}
                onComplete={handleTransferComplete}
            />

            {/* Buttons */}
            <div className="details-buttons">
                <button className="transfer-btn back-btn" onClick={onBack}>
                    ← Go Back
                </button>

                {selectedPlaylist && isFromAuthenticated && isToAuthenticated && (
                    <button className="transfer-btn active" onClick={handleCreate}>
                        Create Playlist
                    </button>
                )}
            </div>
        </div>
    );
}