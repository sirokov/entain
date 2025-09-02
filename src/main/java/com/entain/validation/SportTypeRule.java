package com.entain.validation;

import com.entain.config.SportsConfig;
import com.entain.data.SportEvent;
import com.entain.data.SportType;
import com.entain.exception.InvalidStatusChangeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.entain.config.EntainConstant.ACCEPTED_VALUES;
import static com.entain.config.EntainConstant.INVALID_SPORT;

/**
 * Validation rule that ensures a {@link SportEvent} has a valid sport type.
 * <p>
 * The rule checks against both the predefined {@link SportType} enum values
 * and any additional sport types defined in {@link SportsConfig}.
 * </p>
 *
 * <p>Throws {@link InvalidStatusChangeException} if the sport type is invalid.</p>
 */
@Component
@RequiredArgsConstructor
public class SportTypeRule implements BasicEventValidationRule {

    private final SportsConfig config;

    @Override
    public void validate(SportEvent event) {
        String sport = event.sport();
        boolean isValid = Arrays.stream(SportType.values())
                .anyMatch(s -> s.name().equalsIgnoreCase(sport));

        if (!isValid && config.getExtraTypes() != null) {
            isValid = config.getExtraTypes().stream()
                    .anyMatch(s -> s.equalsIgnoreCase(sport));
        }

        if (!isValid) {
            throw new InvalidStatusChangeException(
                    INVALID_SPORT + sport + ACCEPTED_VALUES +
                            Stream.concat(
                                    Arrays.stream(SportType.values()).map(Enum::name),
                                    config.getExtraTypes() != null ? config.getExtraTypes().stream() : Stream.empty()
                            ).toList()
            );
        }
    }
}