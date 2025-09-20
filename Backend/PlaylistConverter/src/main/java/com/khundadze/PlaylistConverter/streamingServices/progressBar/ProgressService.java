package com.khundadze.PlaylistConverter.streamingServices.progressBar;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendProgress(String transferState, TransferProgress progress) {
        messagingTemplate.convertAndSend("/topic/progress/" + transferState, progress);
    }
}
