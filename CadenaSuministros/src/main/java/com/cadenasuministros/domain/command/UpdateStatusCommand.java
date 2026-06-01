package com.cadenasuministros.domain.command;

import com.cadenasuministros.domain.event.ShipmentStatusChangedEvent;
import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.model.ShipmentEvent;
import com.cadenasuministros.domain.port.out.ShipmentEventRepository;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

public class UpdateStatusCommand extends AbstractShipmentCommand {

    private final String newStatus;

    public UpdateStatusCommand(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository eventRepository,
            ApplicationEventPublisher eventPublisher,
            UUID shipmentId,
            String newStatus) {
        super(shipmentRepository, eventRepository, eventPublisher, shipmentId);
        this.newStatus = newStatus;
    }

    @Override
    protected boolean isNoOp(Shipment current) {
        return current.status().equals(newStatus);
    }

    @Override
    protected void handleNoOp(Shipment current) {
        eventPublisher.publishEvent(new ShipmentStatusChangedEvent(
                shipmentId, current.status(), newStatus));
    }

    @Override
    protected Shipment doExecute(Shipment current) {
        return current.withStatus(newStatus);
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
        eventPublisher.publishEvent(new ShipmentStatusChangedEvent(
                shipmentId, before.status(), after.status()));
    }

    @Override
    protected ShipmentEvent buildUndoEvent(Shipment restored) {
        return new ShipmentEvent(
                UUID.randomUUID(),
                shipmentId,
                newStatus,
                restored.status(),
                restored.currentLocation(),
                restored.currentLocation(),
                Instant.now()
        );
    }

    @Override
    protected void publishUndoEvent(Shipment before, Shipment restored) {
        eventPublisher.publishEvent(new ShipmentStatusChangedEvent(
                shipmentId, newStatus, before.status()));
    }

    @Override
    public String getDescription() {
        return "UpdateStatus: " + shipmentId + " \u2192 " + newStatus;
    }

}
