package com.khundadze.PlaylistConverter.streamingServices.progressBar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransferProgress {
    private String message;
    private int current;
    private int total;
}