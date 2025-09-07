import React, { useState } from "react";
import ServiceSelectionStep from "./ServiceSelectionStep";
import PlaylistDetailsStep from "./PlaylistDetailsStep"; // optional next step
import "./ServiceSelectorMenu.css";

type ServiceSelectorMenuProps = {
    authenticatedServices: string[];
};

const MAX_STEP = 2;

export default function ServiceSelectorMenu({ authenticatedServices }: ServiceSelectorMenuProps)
{
    const [fromService, setFromService] = useState<string | null>(null);
    const [toService, setToService] = useState<string | null>(null);
    const [currentStep, setCurrentStep] = useState<number>(1); // step 1 = selection, step 2 = details

    const goToNextStep = () =>
    {
        if (currentStep < MAX_STEP) setCurrentStep(currentStep + 1);
    };

    const goBack = () =>
    {
        if (currentStep > 1) setCurrentStep(currentStep - 1);
    };

    const onAuthenticate = (serviceId: string) =>
    {
        console.log(`Authenticating ${serviceId}`);
    };


    return (
        <div className="menu-container">
            <h1 className="menu-title">Transfer Playlist</h1>

            {currentStep === 1 && (
                <ServiceSelectionStep
                    authenticatedServices={authenticatedServices}
                    fromService={fromService}
                    toService={toService}
                    setFromService={setFromService}
                    setToService={setToService}
                    onTransferClick={goToNextStep} // pass to trigger step change
                />
            )}

            {currentStep === 2 && (
                <PlaylistDetailsStep
                    fromService={fromService}
                    toService={toService}
                    authenticatedServices={authenticatedServices}
                    onAuthenticate={onAuthenticate}
                    onBack={goBack}
                />
            )}
        </div>
    );
}
