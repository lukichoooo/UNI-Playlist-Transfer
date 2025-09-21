import { useEffect, useState } from "react";
import { converterService, type StreamingPlatform } from "../../services/ConverterService";
import { authService } from "../../services/authService";
import "./Profile.css"; // We will create this CSS file next

export default function Profile()
{
    const [username, setUsername] = useState<string | null>(null);
    const [authenticatedServices, setAuthenticatedServices] = useState<StreamingPlatform[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() =>
    {
        const fetchProfileData = async () =>
        {
            try
            {
                // Get username from JWT
                const userDetails = authService.getUserDetails();
                if (userDetails)
                {
                    setUsername(userDetails.username);
                } else
                {
                    setError("User details not found. Please log in again.");
                    setIsLoading(false);
                    return;
                }

                // Fetch authenticated platforms from the API
                const services = await converterService.getAuthenticatedServices();
                setAuthenticatedServices(services);
            } catch (err)
            {
                console.error("Failed to fetch profile data:", err);
                setError("Failed to load profile data. Please try again.");
            } finally
            {
                setIsLoading(false);
            }
        };

        fetchProfileData();
    }, []);

    const renderServices = () =>
    {
        if (authenticatedServices.length === 0)
        {
            return <p>You are not connected to any streaming services yet.</p>;
        }

        return (
            <ul className="service-list">
                {authenticatedServices.map((service) => (
                    <li key={service} className="service-item">
                        {service}
                    </li>
                ))}
            </ul>
        );
    };

    if (isLoading)
    {
        return <div className="profile-container">Loading profile...</div>;
    }

    if (error)
    {
        return <div className="profile-container error-message">{error}</div>;
    }

    return (
        <div className="profile-container">
            <h1 className="profile-title">Profile Page</h1>
            <div className="profile-card">
                <p><strong>Username:</strong> {username}</p>
            </div>
            <div className="profile-card">
                <h2>Connected Streaming Services</h2>
                {renderServices()}
                <p className="note">
                    Only services with a valid, non-expired token are shown here.
                </p>
            </div>
        </div>
    );
}