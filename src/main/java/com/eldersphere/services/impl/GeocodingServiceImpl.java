package com.eldersphere.services.impl;

import com.eldersphere.dtos.Common.GeocodingResultDTO;
import com.eldersphere.services.GeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Free, no-API-key geocoding via the public OpenStreetMap Nominatim instance. Its usage
 * policy (https://operations.osmfoundation.org/policies/nominatim/) requires a real contact
 * identifier in the User-Agent of every request and caps traffic at roughly 1 request/second
 * — fine for this app's low-volume "resolve an emergency alert's address" and "address
 * autocomplete" use cases, but this must never be called in a tight loop or on every
 * keystroke without the caller debouncing (see the frontend's AddressAutocomplete).
 */
@Slf4j
@Service
public class GeocodingServiceImpl implements GeocodingService {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";
    private static final int SEARCH_LIMIT = 5;

    private final RestTemplate restTemplate;
    private final String userAgent;

    public GeocodingServiceImpl(RestTemplate restTemplate,
                                 @Value("${app.geocoding.contact-email:admin@eldersphere.app}") String contactEmail) {
        this.restTemplate = restTemplate;
        this.userAgent = "ElderSphere/1.0 (" + contactEmail + ")";
    }

    @Override
    public List<GeocodingResultDTO> search(String query) {
        if (query == null || query.trim().length() < 3) {
            return List.of();
        }
        try {
            String url = UriComponentsBuilder.fromUriString(BASE_URL + "/search")
                    .queryParam("q", query.trim())
                    .queryParam("format", "jsonv2")
                    .queryParam("limit", SEARCH_LIMIT)
                    .toUriString();
            List<Map<String, Object>> results = exchange(url, new ParameterizedTypeReference<>() {
            });
            if (results == null) return List.of();
            return results.stream()
                    .map(this::toResult)
                    .filter(r -> r.getDisplayName() != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Geocoding search failed for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    @Override
    public String reverseGeocode(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        try {
            String url = UriComponentsBuilder.fromUriString(BASE_URL + "/reverse")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("format", "jsonv2")
                    .toUriString();
            Map<String, Object> result = exchange(url, new ParameterizedTypeReference<>() {
            });
            return result != null ? (String) result.get("display_name") : null;
        } catch (Exception e) {
            log.warn("Reverse geocoding failed for ({}, {}): {}", latitude, longitude, e.getMessage());
            return null;
        }
    }

    private <T> T exchange(String url, ParameterizedTypeReference<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), type);
        return response.getBody();
    }

    private GeocodingResultDTO toResult(Map<String, Object> raw) {
        return GeocodingResultDTO.builder()
                .displayName((String) raw.get("display_name"))
                .latitude(parseDouble(raw.get("lat")))
                .longitude(parseDouble(raw.get("lon")))
                .build();
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
