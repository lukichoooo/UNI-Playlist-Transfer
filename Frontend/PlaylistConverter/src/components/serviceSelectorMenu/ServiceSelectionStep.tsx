import "./ServiceSelectionStep.css";
import
{
    SpotifyIcon,
    YoutubeIcon,
    SoundcloudIcon,
    DeezerIcon,
    AppleMusicIcon,
} from "../../../public/Icons"; // icon components

type Props = {
    authenticatedServices: string[];
    fromService: string | null;
    toService: string | null;
    setFromService: (s: string | null) => void;
    setToService: (s: string | null) => void;
    onTransferClick: () => void;
};

// Update the array to use the imported icon components
const ALL_SERVICES = [
    { id: "SPOTIFY", label: "Spotify", icon: SpotifyIcon },
    { id: "SOUNDCLOUD", label: "SoundCloud", icon: SoundcloudIcon },
    { id: "YOUTUBE", label: "YouTube", icon: YoutubeIcon },
    { id: "YOUTUBEMUSIC", label: "YT Music", icon: YoutubeIcon }, // Reusing Youtube icon
    { id: "APPLEMUSIC", label: "Apple Music", icon: AppleMusicIcon },
    { id: "DEEZER", label: "Deezer", icon: DeezerIcon },
];

const SERVICE_CLASSES: Record<string, string> = {
    SPOTIFY: "spotify-selected",
    SOUNDCLOUD: "soundcloud-selected",
    YOUTUBE: "youtube-selected",
    YOUTUBEMUSIC: "youtubemusic-selected",
    APPLEMUSIC: "applemusic-selected",
    DEEZER: "deezer-selected",
};

export default function ServiceSelectionStep({
    authenticatedServices,
    fromService,
    toService,
    setFromService,
    setToService,
    onTransferClick,
}: Props)
{
    const handleFromClick = (serviceId: string) =>
        setFromService(fromService === serviceId ? null : serviceId);

    const handleToClick = (serviceId: string) =>
        setToService(toService === serviceId ? null : serviceId);

    const renderServiceBox = (
        service: (typeof ALL_SERVICES)[0],
        selectedService: string | null,
        onClick: (id: string) => void
    ) =>
    {
        const isConnected = authenticatedServices.includes(service.id);
        const isSelected = selectedService === service.id;
        const IconComponent = service.icon; // Get the component from the object

        return (
            <div
                key={service.id}
                className={`service-box ${isConnected ? "connected" : "not-connected"
                    } ${isSelected ? `selected ${SERVICE_CLASSES[service.id]}` : ""}`}
                onClick={() => onClick(service.id)}
            >
                <div className="service-logo">
                    <IconComponent /> {/* Render the icon component */}
                </div>
                <span className="service-label">{service.label}</span>
            </div>
        );
    };

    return (
        <>
            <div className="dual-menus">
                <div className="menu-column">
                    <h2 className="menu-subtitle">FROM</h2>
                    <div className="service-grid">
                        {ALL_SERVICES.map((service) =>
                            renderServiceBox(service, fromService, handleFromClick)
                        )}
                    </div>
                </div>

                <div className="divider"></div>

                <div className="menu-column">
                    <h2 className="menu-subtitle">TO</h2>
                    <div className="service-grid">
                        {ALL_SERVICES.map((service) =>
                            renderServiceBox(service, toService, handleToClick)
                        )}
                    </div>
                </div>
            </div>

            <button
                className={`transfer-btn ${fromService && toService
                    ? "active"
                    : fromService || toService
                        ? "partial"
                        : "disabled"
                    }`}
                onClick={onTransferClick}
                disabled={!(fromService && toService)}
            >
                {fromService && toService
                    ? `Transfer from ${fromService} → ${toService}`
                    : fromService
                        ? "Select destination"
                        : toService
                            ? "Select source"
                            : "Select service"}
            </button>
        </>
    );
}
