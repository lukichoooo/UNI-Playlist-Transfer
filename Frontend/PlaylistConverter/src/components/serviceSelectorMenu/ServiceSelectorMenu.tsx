import { useState, useEffect } from "react";
import ServiceSelectionStep from "./ServiceSelectionStep";
import PlaylistDetailsStep from "./PlaylistDetailsStep";
import "./ServiceSelectorMenu.css";
import { converterService } from "../../services/ConverterService";

const MAX_STEP = 2;

export default function ServiceSelectorMenu()
{
    const [fromService, setFromService] = useState<string | null>(null);
    const [toService, setToService] = useState<string | null>(null);
    const [currentStep, setCurrentStep] = useState<number>(1);
    const [authenticated, setAuthenticated] = useState<string[]>([]);

    useEffect(() =>
    {
        refreshAuthenticatedServices();
    }, []);

    const goToNextStep = () =>
    {
        if (currentStep < MAX_STEP) setCurrentStep(currentStep + 1);
    };

    const goBack = () =>
    {
        if (currentStep > 1) setCurrentStep(currentStep - 1);
    };

    const refreshAuthenticatedServices = async () =>
    {
        try
        {
            const updatedServices = await converterService.getAuthenticatedServices();
            setAuthenticated(updatedServices.map(p => p.toUpperCase()));
        } catch (err)
        {
            console.error("Failed to refresh authenticated services:", err);
        }
    };

    return (
        <div className="menu-container">
            <h1 className="menu-title">Transfer Playlist</h1>

            {currentStep === 1 && (
                <ServiceSelectionStep
                    authenticatedServices={authenticated}
                    fromService={fromService}
                    toService={toService}
                    setFromService={setFromService}
                    setToService={setToService}
                    onTransferClick={goToNextStep}
                />
            )}

            {currentStep === 2 && (
                <PlaylistDetailsStep
                    fromService={fromService}
                    toService={toService}
                    authenticatedServices={authenticated}
                    refreshAuthenticatedServices={refreshAuthenticatedServices}
                    onBack={goBack}
                />
            )}
        </div>
    );
}