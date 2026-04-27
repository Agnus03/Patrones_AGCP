package com.cadenasuministros.adapters.inbound.rest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;
import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final TrackShipmentUseCase trackShipmentUseCase;

    public ShipmentController(TrackShipmentUseCase trackShipmentUseCase) {
        this.trackShipmentUseCase = trackShipmentUseCase;
    }

    @GetMapping
    public List<Shipment> listAll() {
        return trackShipmentUseCase.listAll();
    }

    @GetMapping("/{id}")
    public Shipment getById(@PathVariable UUID id) {
        return trackShipmentUseCase.getById(id);
    }

    @PostMapping
    public Shipment create(@RequestBody ShipmentRequest request) {
        UUID productId = request.productId() != null && !request.productId().isEmpty()
            ? UUID.fromString(request.productId())
            : UUID.randomUUID();
        
        Shipment shipment = new Shipment(
            UUID.randomUUID(),
            productId,
            request.status() != null ? request.status() : "PENDING",
            request.currentLocation() != null ? request.currentLocation() : "WAREHOUSE",
            Instant.now()
        );
        return trackShipmentUseCase.create(shipment);
    }

    public record ShipmentRequest(
        String productId,
        String status,
        String currentLocation
    ) {}
        
}
