package com.entain.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SportEvent {
    private UUID id;
    private String name;
    private String sport;
    private EventStatus status;
    private LocalDateTime startTime;
}
