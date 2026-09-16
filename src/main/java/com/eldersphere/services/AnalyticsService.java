package com.eldersphere.services;

import com.eldersphere.dtos.Analytics.BookingTimeseriesPointDTO;
import com.eldersphere.dtos.Analytics.CaretakerBookingsRatingTrendDTO;
import com.eldersphere.dtos.Analytics.CaretakerLeaderboardEntryDTO;
import com.eldersphere.dtos.Analytics.FamilySpendingSummaryDTO;
import com.eldersphere.dtos.Analytics.RevenueTimeseriesPointDTO;
import com.eldersphere.exceptions.GenericException;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    List<BookingTimeseriesPointDTO> getBookingTimeseries(LocalDate start, LocalDate end, String granularity) throws GenericException;

    List<RevenueTimeseriesPointDTO> getRevenueTimeseries(LocalDate start, LocalDate end, String granularity) throws GenericException;

    List<CaretakerLeaderboardEntryDTO> getCaretakerLeaderboard(int limit, String sortBy) throws GenericException;

    FamilySpendingSummaryDTO getMyFamilySpending(Long familyUserId) throws GenericException;

    List<RevenueTimeseriesPointDTO> getMyCaretakerEarnings(Long callerUserId) throws GenericException;

    CaretakerBookingsRatingTrendDTO getMyCaretakerBookingsRatingTrend(Long callerUserId) throws GenericException;
}
