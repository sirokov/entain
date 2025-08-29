package com.entain.service;

import com.entain.config.SportsConfig;
import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.data.SportType;
import com.entain.data.access.SportEventDAO;
import com.entain.exception.EventNotFoundException;
import com.entain.exception.InvalidStatusChangeException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.entain.config.EntainConstant.*;

@Service
public class SportEventService {

    private final SportEventDAO sportEventDAO;
    private final SseEmitterService sseEmitterService;
    private final SportsConfig sportsConfig;

    public SportEventService(SportEventDAO sportEventDAO,
                             SseEmitterService sseEmitterService,
                             SportsConfig sportsConfig) {
        this.sportEventDAO = sportEventDAO;
        this.sseEmitterService = sseEmitterService;
        this.sportsConfig = sportsConfig;
    }

    public SportEvent createEvent(SportEvent event) {
        validateSport(event.getSport());
        event.setId(UUID.randomUUID());
        sportEventDAO.save(event);
        sseEmitterService.emitUpdate(event);
        return event;
    }

    public List<SportEvent> getEvents(EventStatus status, String sport) {
        return sportEventDAO.findAll(status, sport);
    }

    public SportEvent getEventById(UUID id) {
        return sportEventDAO.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    public SportEvent changeStatus(UUID id, EventStatus newStatus) {
        SportEvent event = getEventById(id);
        validateStatusChange(event, newStatus);
        sportEventDAO.updateStatus(id, newStatus);
        event.setStatus(newStatus);
        sseEmitterService.emitUpdate(event);
        return event;
    }

    private void validateStatusChange(SportEvent event, EventStatus newStatus) {
        switch (event.getStatus()) {
            case INACTIVE -> {
                if (newStatus == EventStatus.FINISHED)
                    throw new InvalidStatusChangeException();
                if (newStatus == EventStatus.ACTIVE && event.getStartTime().isBefore(LocalDateTime.now()))
                    throw new InvalidStatusChangeException();
            }
            case ACTIVE -> {
                if (newStatus != EventStatus.FINISHED)
                    throw new InvalidStatusChangeException();
            }
            case FINISHED -> throw new InvalidStatusChangeException();
        }
    }

    private void validateSport(String sport) {
        if (sport == null || sport.isBlank()) {
            throw new InvalidStatusChangeException(SPORT_NOT_EMPTY_ERROR);
        }

        boolean isValid = false;

        for (SportType type : SportType.values()) {
            if (type.name().equalsIgnoreCase(sport)) {
                isValid = true;
                break;
            }
        }

        if (!isValid && sportsConfig.getExtraTypes() != null
                && sportsConfig.getExtraTypes().stream().anyMatch(s -> s.equalsIgnoreCase(sport))) {
            isValid = true;
        }

        if (!isValid) {
            throw new InvalidStatusChangeException(
                    INVALID_SPORT + sport + ACCEPTED_VALUES +
                            Stream.concat(
                                    Arrays.stream(SportType.values()).map(Enum::name),
                                    sportsConfig.getExtraTypes() != null ? sportsConfig.getExtraTypes().stream() : Stream.empty()
                            ).toList()
            );
        }
    }
}
