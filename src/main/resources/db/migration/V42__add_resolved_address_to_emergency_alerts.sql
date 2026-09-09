-- Free-text address resolved from an alert's lat/long via OpenStreetMap Nominatim
-- (best-effort, see com.eldersphere.services.impl.GeocodingServiceImpl) — lets responders
-- see a readable location instead of just raw coordinates.
ALTER TABLE emergency_alerts
    ADD COLUMN resolved_address TEXT;
