package com.skala.ikgeoljune.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/** §1.1 일시는 ISO 8601(예: 2026-09-03T14:30:00+09:00) 형식으로 내려간다. */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
