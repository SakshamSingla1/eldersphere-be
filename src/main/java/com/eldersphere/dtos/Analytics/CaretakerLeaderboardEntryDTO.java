package com.eldersphere.dtos.Analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaretakerLeaderboardEntryDTO {
    private Long caretakerId;
    private String fullName;
    private Double ratingAverage;
    private long completedBookingCount;
}
