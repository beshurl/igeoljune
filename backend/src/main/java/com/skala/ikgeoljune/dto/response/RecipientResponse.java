package com.skala.ikgeoljune.dto.response;

import com.skala.ikgeoljune.domain.Recipient;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RecipientResponse {
    private Long id;
    private String name;
    private String relationship;
    private LocalDate upcomingAnniversary;

    public static RecipientResponse from(Recipient r) {
        return RecipientResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .relationship(r.getRelationship())
                .upcomingAnniversary(r.getUpcomingAnniversary())
                .build();
    }
}
