// TransferProgress.tsx

import { useState, useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import "./TransferProgress.css";
import { authService } from "../../services/authService";

const VITE_BASE_URL = import.meta.env.VITE_BASE_URL;

type ProgressState = {
    message: string;
    current: number;
    total: number;
};

// Define props for the component
type TransferProgressProps = {
    show: boolean;
    transferState: string;
    onComplete: () => void; // ✅ Add a callback prop
};

const INITIAL_STATE: ProgressState = { message: "Initializing...", current: 0, total: 0 };

export default function TransferProgress({ show, transferState, onComplete }: TransferProgressProps)
{
    const [progress, setProgress] = useState<ProgressState>(INITIAL_STATE);

    useEffect(() =>
    {
        if (!show || !transferState)
        {
            return;
        }

        setProgress(INITIAL_STATE);

        const socket = new SockJS(`${VITE_BASE_URL}/ws`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                Authorization: `Bearer ${authService.getToken()}`,
            },
            onConnect: () =>
            {
                console.log("STOMP client connected");
                stompClient.subscribe(`/topic/progress/${transferState}`, (message) =>
                {
                    const newProgress: ProgressState = JSON.parse(message.body);
                    setProgress(newProgress);

                    if (newProgress.message.toLowerCase().startsWith("completed"))
                    {
                        // ✅ Call the parent's function instead of mutating the prop
                        onComplete();
                    }
                });
            },
            onStompError: (frame) =>
            {
                console.error("Broker reported error: " + frame.headers["message"]);
                console.error("Additional details: " + frame.body);
                setProgress({ ...INITIAL_STATE, message: "Connection error." });
            },
        });

        stompClient.activate();

        return () =>
        {
            if (stompClient.connected)
            {
                console.log("STOMP client disconnecting");
                stompClient.deactivate();
            }
        };
        // Add onComplete to the dependency array
    }, [show, transferState, onComplete]);

    const percentage = progress.total > 0 ? Math.round((progress.current / progress.total) * 100) : 0;

    if (!show)
    {
        return null;
    }

    return (
        <div className="progress-overlay">
            <div className="progress-container">
                <h3 className="progress-title">Transferring Files...</h3>
                <p className="progress-message" title={progress.message}>
                    {progress.message}
                </p>
                <progress className="progress-bar" value={progress.current} max={progress.total} />
                <div className="progress-details">
                    <span className="progress-percentage">{percentage}%</span>
                    <span className="progress-count">
                        {progress.current} / {progress.total}
                    </span>
                </div>
            </div>
        </div>
    );
}