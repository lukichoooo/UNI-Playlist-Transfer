import React from "react";
import "./ServiceSelectionStep.css";

type Props = {
    authenticatedServices: string[];
    fromService: string | null;
    toService: string | null;
    setFromService: (s: string | null) => void;
    setToService: (s: string | null) => void;
    onTransferClick: () => void; // <-- added prop
};

const ALL_SERVICES = [
    { id: "SPOTIFY", label: "Spotify", icon: "/icons/spotify.png" },
    { id: "SOUNDCLOUD", label: "SoundCloud", icon: "/icons/soundcloud.png" },
    { id: "YOUTUBE", label: "YouTube", icon: "/icons/youtube.png" },
    { id: "YOUTUBEMUSIC", label: "YT Music", icon: "/icons/youtubemusic.png" },
    { id: "APPLEMUSIC", label: "Apple Music", icon: "/icons/applemusic.png" },
    { id: "DEEZER", label: "Deezer", icon: "/icons/deezer.png" },
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
    onTransferClick, // <-- receive prop
}: Props)
{
    const handleFromClick = (serviceId: string) =>
        setFromService(fromService === serviceId ? null : serviceId);

    const handleToClick = (serviceId: string) =>
        setToService(toService === serviceId ? null : serviceId);

    // Helper function to render a service box (both TO and FROM)
    const renderServiceBox = (
        service: typeof ALL_SERVICES[0],
        selectedService: string | null,
        onClick: (id: string) => void
    ) =>
    {
        const isConnected = authenticatedServices.includes(service.id);
        const isSelected = selectedService === service.id;

        return (
            <div
                key={service.id}
                className={`service-box ${isConnected ? "connected" : "not-connected"} ${isSelected ? `selected ${SERVICE_CLASSES[service.id]}` : ""
                    }`}
                onClick={() => onClick(service.id)}
            >
                <img src={service.icon} alt={service.label} className="service-logo" />
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
                        {ALL_SERVICES.map((service) => renderServiceBox(service, fromService, handleFromClick))}
                    </div>
                </div>

                <div className="divider"></div>

                <div className="menu-column">
                    <h2 className="menu-subtitle">TO</h2>
                    <div className="service-grid">
                        {ALL_SERVICES.map((service) => renderServiceBox(service, toService, handleToClick))}
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
                onClick={onTransferClick} // <-- use the passed prop
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
