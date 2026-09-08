package com.eldersphere.controllers;

import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Service health check")
public class HealthController {

    @Operation(summary = "Health check")
    @GetMapping
    public ResponseEntity<ResponseModel<Map<String, Object>>> health() {
        return ApiResponse.successResponse(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        ), "ElderSphere backend is healthy");
    }
}
