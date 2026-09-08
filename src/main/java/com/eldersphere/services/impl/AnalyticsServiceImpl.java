package com.eldersphere.services.impl;

import com.eldersphere.dao.booking.BookingDao;
import com.eldersphere.dao.caretaker.CaretakerProfileDao;
import com.eldersphere.dtos.Analytics.BookingTimeseriesPointDTO;
import com.eldersphere.dtos.Analytics.CaretakerLeaderboardEntryDTO;
import com.eldersphere.dtos.Analytics.RevenueTimeseriesPointDTO;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Set<String> ALLOWED_GRANULARITY = Set.of("day", "week");
    private static final Set<String> ALLOWED_SORT_BY = Set.of("RATING", "COMPLETED_BOOKINGS");

    private final BookingDao bookingDao;
    private final CaretakerProfileDao caretakerProfileDao;

    @Override
    public List<BookingTimeseriesPointDTO> getBookingTimeseries(LocalDate start, LocalDate end, String granularity) throws GenericException {
        String unit = validateRange(start, end, granularity);

        Map<LocalDate, Map<String, Long>> byBucket = new LinkedHashMap<>();
        for (Object[] row : bookingDao.bookingTimeseriesRaw(unit, start, end)) {
            LocalDate bucket = toLocalDate(row[0]);
            String status = String.valueOf(row[1]);
            long count = ((Number) row[2]).longValue();
            byBucket.computeIfAbsent(bucket, k -> new LinkedHashMap<>()).put(status, count);
        }

        return byBucket.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> BookingTimeseriesPointDTO.builder()
                        .bucketStart(e.getKey())
                        .total(e.getValue().values().stream().mapToLong(Long::longValue).sum())
                        .byStatus(e.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RevenueTimeseriesPointDTO> getRevenueTimeseries(LocalDate start, LocalDate end, String granularity) throws GenericException {
        String unit = validateRange(start, end, granularity);

        return bookingDao.revenueTimeseriesRaw(unit, start, end).stream()
                .map(row -> RevenueTimeseriesPointDTO.builder()
                        .bucketStart(toLocalDate(row[0]))
                        .revenue((BigDecimal) row[1])
                        .build())
                .sorted(Comparator.comparing(RevenueTimeseriesPointDTO::getBucketStart))
                .toList();
    }

    @Override
    public List<CaretakerLeaderboardEntryDTO> getCaretakerLeaderboard(int limit, String sortBy) throws GenericException {
        String metric = sortBy == null ? "RATING" : sortBy.toUpperCase();
        if (!ALLOWED_SORT_BY.contains(metric)) {
            throw new GenericException(ExceptionCodeEnum.BAD_REQUEST, "sortBy must be one of " + ALLOWED_SORT_BY);
        }
        int cappedLimit = Math.max(1, Math.min(limit, 100));

        List<CaretakerLeaderboardEntryDTO> entries = caretakerProfileDao.leaderboardRaw().stream()
                .map(row -> CaretakerLeaderboardEntryDTO.builder()
                        .caretakerId(((Number) row[0]).longValue())
                        .fullName((String) row[1])
                        .ratingAverage(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                        .completedBookingCount(((Number) row[3]).longValue())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        Comparator<CaretakerLeaderboardEntryDTO> comparator = "COMPLETED_BOOKINGS".equals(metric)
                ? Comparator.comparingLong(CaretakerLeaderboardEntryDTO::getCompletedBookingCount).reversed()
                : Comparator.comparing(CaretakerLeaderboardEntryDTO::getRatingAverage,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();

        return entries.stream()
                .sorted(comparator)
                .limit(cappedLimit)
                .toList();
    }

    private String validateRange(LocalDate start, LocalDate end, String granularity) throws GenericException {
        if (start == null || end == null || start.isAfter(end)) {
            throw new GenericException(ExceptionCodeEnum.BAD_REQUEST, "start must be on or before end");
        }
        String unit = granularity == null ? "day" : granularity.toLowerCase();
        if (!ALLOWED_GRANULARITY.contains(unit)) {
            throw new GenericException(ExceptionCodeEnum.BAD_REQUEST, "granularity must be one of " + ALLOWED_GRANULARITY);
        }
        return unit;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Date sqlDate) return sqlDate.toLocalDate();
        if (value instanceof java.time.LocalDateTime ldt) return ldt.toLocalDate();
        if (value instanceof LocalDate localDate) return localDate;
        throw new IllegalStateException("Unexpected bucket date type: " + (value == null ? "null" : value.getClass()));
    }
}
