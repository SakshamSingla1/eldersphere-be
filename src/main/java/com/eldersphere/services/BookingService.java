package com.eldersphere.services;

import com.eldersphere.dtos.Booking.AvailableSlotsResponse;
import com.eldersphere.dtos.Booking.BookingRequest;
import com.eldersphere.dtos.Booking.BookingResponse;
import com.eldersphere.enums.BookingStatusEnum;
import com.eldersphere.exceptions.GenericException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingResponse create(BookingRequest request) throws GenericException;
    BookingResponse updateStatus(Long id, BookingStatusEnum status) throws GenericException;
    BookingResponse getById(Long id) throws GenericException;
    Page<BookingResponse> search(Long familyUserId, Long caretakerId, BookingStatusEnum status, Pageable pageable);

    /**
     * All bookings sharing the given recurring_group_id, oldest first.
     */
    List<BookingResponse> getByRecurringGroupId(String recurringGroupId) throws GenericException;

    /**
     * Cancels every booking in a recurring series. Restricted to the series'
     * own family member or an ADMIN/SUPER_ADMIN caller.
     */
    List<BookingResponse> cancelSeries(String recurringGroupId, Long requestingUserId) throws GenericException;

    /**
     * Candidate booking slots for a caretaker on a given date and service, generated on a
     * 30-minute grid across their declared weekly availability windows (or a default
     * 07:00-20:00 window if they haven't declared any), each flagged available/unavailable
     * against their existing bookings that day.
     */
    AvailableSlotsResponse getAvailableSlots(Long caretakerId, LocalDate date, Long serviceId) throws GenericException;
}
