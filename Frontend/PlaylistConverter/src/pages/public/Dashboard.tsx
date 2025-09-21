import { useEffect, useState, useMemo } from "react";
import { converterService, type StreamingPlatform } from "../../services/ConverterService";
import type { PlaylistSearchDto } from "../../types";
import Fuse from "fuse.js";
import styles from "./Dashboard.module.css";
import { useNavigate } from "react-router-dom";

const ALL_SERVICES: StreamingPlatform[] = [
    "SPOTIFY",
    "SOUNDCLOUD",
    "YOUTUBE",
    "YOUTUBEMUSIC",
    "APPLEMUSIC",
    "DEEZER",
];
const PLAYLISTS_PER_PAGE = 10;

type PlaylistWithPlatform = PlaylistSearchDto & { platform: StreamingPlatform };

export default function Dashboard()
{
    const [authenticatedServices, setAuthenticatedServices] = useState<StreamingPlatform[]>([]);
    const [selectedPlatforms, setSelectedPlatforms] = useState<StreamingPlatform[]>([]);
    const [playlistsByPlatform, setPlaylistsByPlatform] = useState<Record<StreamingPlatform, PlaylistSearchDto[]>>({
        SPOTIFY: [],
        SOUNDCLOUD: [],
        YOUTUBE: [],
        YOUTUBEMUSIC: [],
        APPLEMUSIC: [],
        DEEZER: [],
    });
    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState<Record<StreamingPlatform, string>>({
        SPOTIFY: "",
        SOUNDCLOUD: "",
        YOUTUBE: "",
        YOUTUBEMUSIC: "",
        APPLEMUSIC: "",
        DEEZER: "",
    });

    const [searchQuery, setSearchQuery] = useState("");
    const [currentPage, setCurrentPage] = useState(1);

    const navigate = useNavigate();

    const fetchAuthenticatedServices = async () =>
    {
        try
        {
            const services = await converterService.getAuthenticatedServices();
            setAuthenticatedServices(services);
        } catch (err)
        {
            console.error("Failed to fetch authenticated services:", err);
        }
    };

    useEffect(() =>
    {
        fetchAuthenticatedServices();
    }, []);

    useEffect(() =>
    {
        const fetchPlaylists = async (platforms: StreamingPlatform[]) =>
        {
            setIsLoading(true);
            setErrors(Object.fromEntries(platforms.map(p => [p, ""])) as Record<StreamingPlatform, string>);
            const newPlaylistsByPlatform: Record<StreamingPlatform, PlaylistSearchDto[]> = { ...playlistsByPlatform };

            const fetchPromises = platforms.map(async (platform) =>
            {
                try
                {
                    const fetchedPlaylists = await converterService.getPlaylists(platform);
                    newPlaylistsByPlatform[platform] = fetchedPlaylists;
                } catch (err: any)
                {
                    console.error(`Failed to fetch playlists for ${platform}:`, err);
                    if (err.response?.status === 403)
                    {
                        setErrors(prev => ({ ...prev, [platform]: `Not authenticated. Please connect your ${platform} account.` }));
                    } else
                    {
                        setErrors(prev => ({ ...prev, [platform]: "Failed to load playlists." }));
                    }
                    newPlaylistsByPlatform[platform] = [];
                }
            });

            await Promise.all(fetchPromises);
            setPlaylistsByPlatform(newPlaylistsByPlatform);
            setIsLoading(false);
            setCurrentPage(1);
        };

        fetchPlaylists(selectedPlatforms);
    }, [selectedPlatforms]);

    const handleAuthenticate = async (platform: StreamingPlatform) =>
    {
        try
        {
            await converterService.oauthLogin(platform);
            await fetchAuthenticatedServices();
        } catch (error)
        {
            console.error("Authentication failed:", error);
        }
    };

    const handlePlatformToggle = (platform: StreamingPlatform) =>
    {
        const updatedSelection = selectedPlatforms.includes(platform)
            ? selectedPlatforms.filter(p => p !== platform)
            : [...selectedPlatforms, platform];
        setSelectedPlatforms(updatedSelection);
    };

    const allPlaylists: PlaylistWithPlatform[] = useMemo(() =>
    {
        return Object.entries(playlistsByPlatform)
            .filter(([platform, _]) => selectedPlatforms.includes(platform as StreamingPlatform))
            .flatMap(([platform, playlists]) =>
                playlists.map(p => ({ ...p, platform: platform as StreamingPlatform }))
            );
    }, [playlistsByPlatform, selectedPlatforms]);

    const fuse = useMemo(
        () => new Fuse(allPlaylists, {
            keys: ["name", "platform"],
            threshold: 0.3,
            ignoreLocation: true,
        }),
        [allPlaylists]
    );

    const filteredPlaylists = useMemo(() =>
    {
        if (!searchQuery)
        {
            return allPlaylists;
        }
        return fuse.search(searchQuery).map(result => result.item);
    }, [searchQuery, allPlaylists, fuse]);

    const totalPages = Math.ceil(filteredPlaylists.length / PLAYLISTS_PER_PAGE);
    const paginatedPlaylists = useMemo(() =>
    {
        const startIndex = (currentPage - 1) * PLAYLISTS_PER_PAGE;
        const endIndex = startIndex + PLAYLISTS_PER_PAGE;
        return filteredPlaylists.slice(startIndex, endIndex);
    }, [filteredPlaylists, currentPage]);

    const handlePrevPage = () =>
    {
        setCurrentPage(prev => Math.max(prev - 1, 1));
    };

    const handleNextPage = () =>
    {
        setCurrentPage(prev => Math.min(prev + 1, totalPages));
    };

    const handleTransferClick = (platform: StreamingPlatform, playlist: PlaylistSearchDto) =>
    {
        navigate("/", { state: { fromService: platform, selectedPlaylist: playlist } });
    };

    return (
        <div className={styles.dashboardContainer}>
            <h1 className={styles.dashboardTitle}>Your Playlists</h1>
            <p className={styles.dashboardDescription}>Select one or more streaming services to search your playlists. Click the service icon to toggle selection.</p>

            <div className={styles.serviceSelection}>
                {ALL_SERVICES.map((platform) =>
                {
                    const isAuthenticated = authenticatedServices.includes(platform);
                    return (
                        <button
                            key={platform}
                            className={`${styles.serviceButton} ${selectedPlatforms.includes(platform) ? styles.selected : ""} ${isAuthenticated ? styles.authenticated : ""}`}
                            onClick={() => isAuthenticated ? handlePlatformToggle(platform) : handleAuthenticate(platform)}
                        >
                            {platform}
                            {isAuthenticated && <span className={styles.checkIcon}>✔</span>}
                        </button>
                    );
                })}
            </div>

            <div className={styles.searchSection}>
                <input
                    type="text"
                    placeholder="Search for a playlist..."
                    value={searchQuery}
                    onChange={(e) =>
                    {
                        setSearchQuery(e.target.value);
                        setCurrentPage(1);
                    }}
                    className={styles.searchInput}
                />
            </div>

            <div className={styles.playlistSection}>
                {isLoading && <p>Loading playlists...</p>}
                {!isLoading && selectedPlatforms.length === 0 && (
                    <p className={styles.infoMessage}>Select multiple services to begin searching for playlists.</p>
                )}
                {!isLoading && Object.values(errors).some(e => e) && (
                    <div className={styles.errorMessages}>
                        {Object.entries(errors).map(([platform, message]) => message && (
                            <p key={platform} className={styles.errorMessage}>{message}</p>
                        ))}
                    </div>
                )}
                {!isLoading && filteredPlaylists.length > 0 && (
                    <>
                        <ul className={styles.playlistList}>
                            {paginatedPlaylists.map((playlist) => (
                                <li key={playlist.platform + playlist.id} className={styles.playlistItem}>
                                    <div className={styles.playlistInfo}>
                                        <span className={styles.playlistName}>{playlist.name}</span>
                                        <span className={styles.platformName}>{playlist.platform} - {playlist.totalTracks} tracks</span>
                                    </div>
                                    <button
                                        className={styles.transferButton}
                                        onClick={() => handleTransferClick(playlist.platform, playlist)}
                                    >
                                        Transfer
                                    </button>
                                </li>
                            ))}
                        </ul>
                        <div className={styles.paginationControls}>
                            <button
                                onClick={handlePrevPage}
                                disabled={currentPage === 1}
                                className={styles.paginationButton}
                            >
                                Previous
                            </button>
                            <span className={styles.pageInfo}>
                                Page {currentPage} of {totalPages}
                            </span>
                            <button
                                onClick={handleNextPage}
                                disabled={currentPage === totalPages}
                                className={styles.paginationButton}
                            >
                                Next
                            </button>
                        </div>
                    </>
                )}
                {!isLoading && filteredPlaylists.length === 0 && selectedPlatforms.length > 0 && !searchQuery && (
                    <p className={styles.infoMessage}>No playlists found for the selected services.</p>
                )}
                {!isLoading && filteredPlaylists.length === 0 && searchQuery && (
                    <p className={styles.infoMessage}>No playlists match your search query.</p>
                )}
            </div>
        </div>
    );
}