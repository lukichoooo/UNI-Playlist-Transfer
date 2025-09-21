import { useState, useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import "./TransferProgress.css"; // Make sure to update this file with the new CSS
import { authService } from "../../services/authService";

const VITE_BASE_URL = import.meta.env.VITE_BASE_URL;

type LogEntry = {
    message: string;
    current: number;
    total: number;
    found: boolean;
};

type OverallProgress = {
    current: number;
    total: number;
};

type Statistics = {
    transferred: number;
    notFound: number;
    percentage: number;
};

type TransferProgressProps = {
    show: boolean;
    transferState: string;
    onComplete: () => void; // This will now act as the "onClose" function
};

const INITIAL_LOG: LogEntry = { message: "Initializing transfer...", current: 0, total: 0, found: true };
const INITIAL_PROGRESS: OverallProgress = { current: 0, total: 0 };

export default function TransferProgress({ show, transferState, onComplete }: TransferProgressProps)
{
    const [logs, setLogs] = useState<LogEntry[]>([INITIAL_LOG]);
    const [overallProgress, setOverallProgress] = useState<OverallProgress>(INITIAL_PROGRESS);
    const [isComplete, setIsComplete] = useState(false);
    const [stats, setStats] = useState<Statistics>({ transferred: 0, notFound: 0, percentage: 0 });

    const logContainerRef = useRef<HTMLDivElement>(null);

    // Main effect for WebSocket connection
    useEffect(() =>
    {
        if (!show || !transferState)
        {
            return;
        }

        // Reset state completely when the component is re-shown
        setLogs([INITIAL_LOG]);
        setOverallProgress(INITIAL_PROGRESS);
        setIsComplete(false);

        const socket = new SockJS(`${VITE_BASE_URL}/ws`);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: { Authorization: `Bearer ${authService.getToken()}` },
            onConnect: () =>
            {
                stompClient.subscribe(`/topic/progress/${transferState}`, (message) =>
                {
                    const newLog: LogEntry = JSON.parse(message.body);

                    if (newLog.message.toLowerCase().startsWith("completed"))
                    {
                        setIsComplete(true);
                    } else
                    {
                        setLogs(prevLogs => [...prevLogs, newLog]);
                        setOverallProgress({ current: newLog.current, total: newLog.total });
                    }
                });
            },
            onStompError: (frame) =>
            {
                console.error("Broker reported error: " + frame.headers["message"]);
                const errorLog: LogEntry = {
                    message: "Connection error. Please try again.",
                    current: overallProgress.current,
                    total: overallProgress.total,
                    found: false,
                };
                setLogs(prevLogs => [...prevLogs, errorLog]);
                setIsComplete(true); // End on error to show summary
            },
        });

        stompClient.activate();

        return () =>
        {
            if (stompClient.connected)
            {
                stompClient.deactivate();
            }
        };
    }, [show, transferState]);

    // Effect to calculate stats once the transfer is complete
    useEffect(() =>
    {
        if (isComplete)
        {
            // Exclude the initial "Initializing" message from stats
            const actualLogs = logs.slice(1);
            const transferredCount = actualLogs.filter(log => log.found).length;
            const notFoundCount = actualLogs.filter(log => !log.found).length;
            const totalProcessed = transferredCount + notFoundCount;
            const percentage = totalProcessed > 0 ? Math.round((transferredCount / totalProcessed) * 100) : 0;

            setStats({
                transferred: transferredCount,
                notFound: notFoundCount,
                percentage: percentage,
            });
        }
    }, [isComplete, logs]);

    // Effect to auto-scroll the log container
    useEffect(() =>
    {
        if (logContainerRef.current)
        {
            logContainerRef.current.scrollTop = logContainerRef.current.scrollHeight;
        }
    }, [logs]);


    if (!show)
    {
        return null;
    }

    // Helper to determine the summary card's color class
    const getStatusClassName = () =>
    {
        if (!isComplete) return '';
        if (stats.percentage >= 85) return 'status-green';
        if (stats.percentage >= 60) return 'status-orange';
        return 'status-red';
    };

    return (
        <div className="progress-overlay">
            <div className={`progress-container ${getStatusClassName()}`}>
                {!isComplete ? (
                    // --- PROGRESS VIEW ---
                    <>
                        <h3 className="progress-title">Transferring Playlist...</h3>
                        <div className="progress-log-container" ref={logContainerRef}>
                            {logs.map((log, index) => (
                                <p key={index} className={`progress-log-message ${log.found ? 'success' : 'error'}`}>
                                    {log.message}
                                </p>
                            ))}
                        </div>
                        <progress className="progress-bar" value={overallProgress.current} max={overallProgress.total} />
                        <div className="progress-details">
                            <span>{Math.round((overallProgress.current / (overallProgress.total || 1)) * 100)}%</span>
                            <span>{overallProgress.current} / {overallProgress.total}</span>
                        </div>
                    </>
                ) : (
                    // --- COMPLETION SUMMARY VIEW ---
                    <div className="summary-view">
                        <h3 className="summary-title">Transfer Complete!</h3>
                        <div className="summary-stats">
                            <p><strong>Success Rate:</strong> <span className="summary-percentage">{stats.percentage}%</span></p>
                            <div className="summary-details">
                                <span><strong>Transferred:</strong> {stats.transferred}</span>
                                <span><strong>Not Found:</strong> {stats.notFound}</span>
                            </div>
                        </div>
                        <button className="summary-close-button" onClick={onComplete}>
                            Close
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}