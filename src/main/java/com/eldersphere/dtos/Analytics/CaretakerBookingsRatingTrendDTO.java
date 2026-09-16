package com.eldersphere.dtos.Analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaretakerBookingsRatingTrendDTO {
    private List<BookingTimeseriesPointDTO> bookingsPerWeek;
    private List<RatingTrendPointDTO> ratingTrend;
}
