package com.cadenasuministros.domain.command;

import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.model.ShipmentEvent;
import com.cadenasuministros.domain.port.out.ShipmentEventRepository;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractShipmentCommand implements ShipmentCommand {

    protected final ShipmentRepository shipmentRepository;
    protected final ShipmentEventRepository eventRepository;
    protected final ApplicationEventPublisher eventPublisher;
    protected final UUID shipmentId;

    protected Shipment previousState;

    protected AbstractShipmentCommand(
            ShipmentRepository shipmentRepository,
            ShipmentEventRepository eventRepository,
            ApplicationEventPublisher eventPublisher,
            UUID shipmentId) {
        this.shipmentRepository = shipmentRepository;
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
        this.shipmentId = shipmentId;
    }

    @Override
    public Shipment execute() {
        Shipment current = fetchCurrent();
        if (isNoOp(current)) {
            handleNoOp(current);
            return current;
        }
        this.previousState = current;
        Shipment updated = doExecute(current);
        Shipment saved = save(updated);
        recordEvent(current, saved);
        publishEvent(current, saved);
        return saved;
    }

    @Override
    public Optional<Shipment> undo() {
        if (previousState == null) return Optional.empty();
        Shipment restored = doUndo();
        eventRepository.save(buildUndoEvent(restored));
        publishUndoEvent(previousState, restored);
        return Optional.of(restored);
    }

    @Override
    public UUID getShipmentId() {
        return shipmentId;
    }

    protected Shipment fetchCurrent() {
        return shipmentRepository.findShipmentById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));
    }

    protected boolean isNoOp(Shipment current) {
        return false;
    }

    protected void handleNoOp(Shipment current) {
    }

    protected Shipment doExecute(Shipment current) {
        return current;
    }

    protected Shipment save(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }

    protected void recordEvent(Shipment before, Shipment after) {
        eventRepository.save(buildEvent(before, after));
    }

    protected void publishEvent(Shipment before, Shipment after) {
    }

    protected Shipment doUndo() {
        return shipmentRepository.save(previousState);
    }

    protected abstract ShipmentEvent buildEvent(Shipment before, Shipment after);

    protected abstract ShipmentEvent buildUndoEvent(Shipment restored);

    protected abstract void publishUndoEvent(Shipment before, Shipment restored);

}
