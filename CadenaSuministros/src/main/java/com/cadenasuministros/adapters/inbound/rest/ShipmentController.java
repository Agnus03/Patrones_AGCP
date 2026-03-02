package com.cadenasuministros.adapters.inbound.rest;

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

    @GetMapping("/{id}")
    public Shipment getById(@PathVariable UUID id) {
        return trackShipmentUseCase.getById(id);
    }
}
