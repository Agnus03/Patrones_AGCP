package com.cadenasuministros.adapters.inbound.rest;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.port.in.RegisterSensorReadingUseCase;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final RegisterSensorReadingUseCase registerSensorReadingUseCase;

    public SensorController(RegisterSensorReadingUseCase registerSensorReadingUseCase) {
        this.registerSensorReadingUseCase = registerSensorReadingUseCase;
    }

    public record SensorReadingRequest(
            UUID shipmentId,
            Double temperatureC,
            Double humidityPct,
            Double latitude,
            Double longitude
    ) {}

    @PostMapping("/readings")
    public SensorReading create(@RequestBody @Valid SensorReadingRequest req) {
        SensorReading reading = new SensorReading(
                UUID.randomUUID(),
                req.shipmentId(),
                Instant.now(),
                req.temperatureC(),
                req.humidityPct(),
                req.latitude(),
                req.longitude()
        );
        return registerSensorReadingUseCase.register(reading);
    }
    
    @GetMapping
    public List<SensorReading> list() {
        return registerSensorReadingUseCase.listAll();
    }
}
