package com.entain.service;

import com.entain.config.SportsConfig;
import com.entain.data.SportType;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.entain.config.EntainConstant.ALLOWED;
import static com.entain.config.EntainConstant.INVALID_SPORT;

@Service
public class SportValidationService {

    private final Set<String> validSports;

    public SportValidationService(SportsConfig config) {
        Set<String> baseSports = EnumSet.allOf(SportType.class).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        validSports = new HashSet<>(baseSports);
        if (config.getExtraTypes() != null) {
            validSports.addAll(config.getExtraTypes());
        }
    }

    public void validate(String sport) {
        if (!validSports.contains(sport)) {
            throw new IllegalArgumentException(INVALID_SPORT + sport + ALLOWED + validSports);
        }
    }
}
