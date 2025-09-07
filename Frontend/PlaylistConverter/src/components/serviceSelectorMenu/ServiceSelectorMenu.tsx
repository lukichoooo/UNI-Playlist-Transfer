import React, { useState } from "react";
import "./ServiceSelectorMenu.css";

type ServiceSelectorMenuProps = {
    authenticatedServices: string[];
};

const ALL_SERVICES = [
    { id: "spotify", label: "Spotify", icon: "/icons/spotify.png" },
    { id: "soundcloud", label: "SoundCloud", icon: "/icons/soundcloud.png" },
    { id: "youtube", label: "YouTube", icon: "/icons/youtube.png" },
    { id: "youtubemusic", label: "YT Music", icon: "/icons/youtubemusic.png" },
    { id: "applemusic", label: "Apple Music", icon: "/icons/applemusic.png" },
    { id: "deezer", label: "Deezer", icon: "/icons/deezer.png" },
];

const SERVICE_CLASSES: Record<string, string> = {
    spotify: "spotify-selected",
    soundcloud: "soundcloud-selected",
    youtube: "youtube-selected",
    youtubemusic: "youtubemusic-selected",
    applemusic: "applemusic-selected",
    deezer: "deezer-selected",
};

export default function ServiceSelectorMenu({ authenticatedServices }: ServiceSelectorMenuProps)
{
    const [fromService, setFromService] = useState<string | null>(null);
    const [toService, setToService] = useState<string | null>(null);

    function handleFromClick(serviceId: string)
    {
        if (fromService === serviceId)
            setFromService(null);
        else setFromService(serviceId);
    }

    function handleToClick(serviceId: string)
    {
        if (toService === serviceId)
            setToService(null);
        else setToService(serviceId);
    }

    return (
        <div className="menu-container">
            <h1 className="menu-title">Transfer Playlist</h1>
            <div className="dual-menus">
                {/* FROM menu */}
                <div className="menu-column">
                    <h2 className="menu-subtitle">FROM</h2>
                    <div className="service-grid">
                        {ALL_SERVICES.map((service) =>
                        {
                            const isConnected = authenticatedServices.includes(service.id);
                            const isSelected = fromService === service.id;
                            return (
                                <div
                                    key={service.id}
                                    className={`service-box ${isConnected ? "connected" : "not-connected"} ${isSelected ? `selected ${SERVICE_CLASSES[service.id]}` : ""
                                        }`}
                                    onClick={() => handleFromClick(service.id)}
                                >
                                    <img src={service.icon} alt={service.label} className="service-logo" />
                                    <span className="service-label">{service.label}</span>
                                </div>
                            );
                        })}
                    </div>
                </div>

                <div className="divider"></div>

                {/* TO menu */}
                <div className="menu-column">
                    <h2 className="menu-subtitle">TO</h2>
                    <div className="service-grid">
                        {ALL_SERVICES.map((service) =>
                        {
                            const isConnected = authenticatedServices.includes(service.id);
                            const isSelected = toService === service.id;
                            return (
                                <div
                                    key={service.id}
                                    className={`service-box ${isConnected ? "connected" : "not-connected"} ${isSelected ? `selected ${SERVICE_CLASSES[service.id]}` : ""
                                        }`}
                                    onClick={() => handleToClick(service.id)}
                                >
                                    <img src={service.icon} alt={service.label} className="service-logo" />
                                    <span className="service-label">{service.label}</span>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* TRANSFER button */}
            <button
                className={`transfer-btn ${fromService && toService
                        ? "active"
                        : fromService || toService
                            ? "partial"
                            : "disabled"
                    }`}
            >
                {fromService && toService
                    ? `Transfer from ${fromService} → ${toService}`
                    : fromService
                        ? "Select destination"
                        : toService
                            ? "Select source"
                            : "Select service"}
            </button>

        </div>
    );
}
