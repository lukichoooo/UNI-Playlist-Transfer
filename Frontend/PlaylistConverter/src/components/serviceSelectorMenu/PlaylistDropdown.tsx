import { useState, useMemo } from "react";
import type { PlaylistSearchDto } from "../../types";
import "./PlaylistDropdown.css";

type Props = {
    fromPlaylists: PlaylistSearchDto[];
    selectedPlaylistId: string;
    setSelectedPlaylistId: (id: string) => void;
    isOpen: boolean; // controls modal visibility
    onClose: () => void;
};

export function PlaylistDropdown({ fromPlaylists, selectedPlaylistId, setSelectedPlaylistId, isOpen, onClose }: Props)
{
    const [search, setSearch] = useState("");

    const filteredPlaylists = useMemo(() =>
    {
        if (!search) return fromPlaylists;
        let left = 0, right = fromPlaylists.length - 1;
        const result: PlaylistSearchDto[] = [];
        while (left <= right)
        {
            const mid = Math.floor((left + right) / 2);
            const name = fromPlaylists[mid].name.toLowerCase();
            if (name.includes(search.toLowerCase()))
            {
                result.push(fromPlaylists[mid]);
                // neighbors
                let i = mid - 1;
                while (i >= 0 && fromPlaylists[i].name.toLowerCase().includes(search.toLowerCase())) { result.push(fromPlaylists[i]); i--; }
                i = mid + 1;
                while (i < fromPlaylists.length && fromPlaylists[i].name.toLowerCase().includes(search.toLowerCase())) { result.push(fromPlaylists[i]); i++; }
                break;
            } else if (name < search.toLowerCase()) left = mid + 1;
            else right = mid - 1;
        }
        return result;
    }, [search, fromPlaylists]);

    if (!isOpen) return null;

    return (
        <div className="playlist-modal-overlay" onClick={onClose}>
            <div className="playlist-modal" onClick={(e) => e.stopPropagation()}>
                <div className="playlist-modal-header">
                    <h2>Select a Playlist</h2>
                    <button className="close-btn" onClick={onClose}>×</button>
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
                            className={`playlist-item ${selectedPlaylistId === pl.id ? "selected" : ""}`}
                            onClick={() =>
                            {
                                setSelectedPlaylistId(pl.id);
                                onClose();
                                setSearch("");
                            }}
                        >
                            {pl.name}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
