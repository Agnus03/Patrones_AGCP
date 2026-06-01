package com.cadenasuministros.domain.command;

import com.cadenasuministros.domain.event.ShipmentLocationChangedEvent;
import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.model.ShipmentEvent;
import com.cadenasuministros.domain.port.out.ShipmentEventRepository;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

public class UpdateLocationCommand extends AbstractShipmentCommand {

    private final String newLocation;

    public UpdateLocationCommand(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository eventRepository,
            ApplicationEventPublisher eventPublisher,
            UUID shipmentId,
            String newLocation) {
        super(shipmentRepository, eventRepository, eventPublisher, shipmentId);
        this.newLocation = newLocation;
    }

    @Override
    protected boolean isNoOp(Shipment current) {
        return current.currentLocation().equals(newLocation);
    }

    @Override
    protected Shipment doExecute(Shipment current) {
        return current.withLocation(newLocation);
    }

    @Override
    protected ShipmentEvent buildEvent(Shipment before, Shipment after) {
        return new ShipmentEvent(
                UUID.randomUUID(),
                shipmentId,
                before.status(),
                after.status(),
                before.currentLocation(),
                after.currentLocation(),
                Instant.now()
        );
    }

    @Override
    protected void publishEvent(Shipment before, Shipment after) {
        eventPublisher.publishEvent(new ShipmentLocationChangedEvent(
                shipmentId, before.currentLocation(), after.currentLocation()));
    }

    @Override
    protected ShipmentEvent buildUndoEvent(Shipment restored) {
        return new ShipmentEvent(
                UUID.randomUUID(),
                shipmentId,
                restored.status(),
                restored.status(),
                newLocation,
                restored.currentLocation(),
                Instant.now()
        );
    }

    @Override
    protected void publishUndoEvent(Shipment before, Shipment restored) {
        eventPublisher.publishEvent(new ShipmentLocationChangedEvent(
                shipmentId, newLocation, before.currentLocation()));
    }

    @Override
    public String getDescription() {
        return "UpdateLocation: " + shipmentId + " \u2192 " + newLocation;
    }

}
