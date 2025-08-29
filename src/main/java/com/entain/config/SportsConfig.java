package com.entain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sports")
public class SportsConfig {

    private List<String> extraTypes;

    public List<String> getExtraTypes() {
        return extraTypes;
    }

    public void setExtraTypes(List<String> extraTypes) {
        this.extraTypes = extraTypes;
    }
}