import { useState, useMemo } from "react";
import type { PlaylistSearchDto } from "../../types";
import Fuse from "fuse.js";
import "./PlaylistDropdown.css";

type Props = {
    fromPlaylists: PlaylistSearchDto[];
    selectedPlaylist: PlaylistSearchDto | null;
    setSelectedPlaylist: (playlist: PlaylistSearchDto) => void;
    isOpen: boolean;
    onClose: () => void;
};

export function PlaylistDropdown({
    fromPlaylists,
    selectedPlaylist,
    setSelectedPlaylist,
    isOpen,
    onClose,
}: Props)
{
    const [search, setSearch] = useState("");

    // Create Fuse instance once
    const fuse = useMemo(
        () =>
            new Fuse(fromPlaylists, {
                keys: ["name"],
                threshold: 0.3,
                ignoreLocation: true,
            }),
        [fromPlaylists]
    );

    const filteredPlaylists = useMemo(() =>
    {
        if (!search) return fromPlaylists;
        return fuse.search(search).map((r) => r.item);
    }, [search, fromPlaylists, fuse]);

    if (!isOpen) return null;

    return (
        <div className="playlist-modal-overlay" onClick={onClose}>
            <div className="playlist-modal" onClick={(e) => e.stopPropagation()}>
                <div className="playlist-modal-header">
                    <h2>Select a Playlist</h2>
                    <button className="close-btn" onClick={onClose}>
                        ×
                    </button>
                </div>
                <input
                    className="playlist-search"
                    type="text"
                    placeholder="Search playlists..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
                <div className="playlist-list">
                    {filteredPlaylists.length === 0 && <div className="playlist-item">No results</div>}
                    {filteredPlaylists.map((pl) => (
                        <div
                            key={pl.id}
                            className={`playlist-item ${selectedPlaylist?.id === pl.id ? "selected" : ""
                                }`}
                            onClick={() =>
                            {
                                setSelectedPlaylist(pl);
                                onClose();
                                setSearch("");
                            }}
                        >
                            <span className="playlist-name">{pl.name}</span>
                            <span className="playlist-count">
                                {pl.totalTracks} {pl.totalTracks === 1 ? "track" : "tracks"}
                            </span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
