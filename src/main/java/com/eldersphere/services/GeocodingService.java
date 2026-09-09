package com.eldersphere.services;

import com.eldersphere.dtos.Common.GeocodingResultDTO;

import java.util.List;

public interface GeocodingService {

    /** Forward geocoding — free-text address search, e.g. for an address-autocomplete field. */
    List<GeocodingResultDTO> search(String query);

    /** Reverse geocoding — turns a lat/long pair into a readable address, or null if it can't be resolved. */
    String reverseGeocode(Double latitude, Double longitude);
}
