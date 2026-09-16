package com.eldersphere.dtos.Analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingTrendPointDTO {
    private LocalDate bucketStart;
    private Double averageRating;
    private long reviewCount;
}
