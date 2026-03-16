package com.cadenasuministros.domain.model;

import java.time.Instant;
import java.util.UUID;

public class DeliveryReport {

    private final UUID reportId;
    private final UUID shipmentId;
    private final UUID productId;
    private final String origin;
    private final String destination;
    private final Instant dispatchTime;
    private final Instant deliveryTime;
    private final Double averageTemperature;
    private final Double averageHumidity;
    private final Boolean temperatureAlert;
    private final Boolean humidityAlert;
    private final String deliveryStatus;
    private final String observations;

    private DeliveryReport(Builder builder) {
        this.reportId = builder.reportId;
        this.shipmentId = builder.shipmentId;
        this.productId = builder.productId;
        this.origin = builder.origin;
        this.destination = builder.destination;
        this.dispatchTime = builder.dispatchTime;
        this.deliveryTime = builder.deliveryTime;
        this.averageTemperature = builder.averageTemperature;
        this.averageHumidity = builder.averageHumidity;
        this.temperatureAlert = builder.temperatureAlert;
        this.humidityAlert = builder.humidityAlert;
        this.deliveryStatus = builder.deliveryStatus;
        this.observations = builder.observations;
    }

    public UUID getReportId() { return reportId; }
    public UUID getShipmentId() { return shipmentId; }
    public UUID getProductId() { return productId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public Instant getDispatchTime() { return dispatchTime; }
    public Instant getDeliveryTime() { return deliveryTime; }
    public Double getAverageTemperature() { return averageTemperature; }
    public Double getAverageHumidity() { return averageHumidity; }
    public Boolean getTemperatureAlert() { return temperatureAlert; }
    public Boolean getHumidityAlert() { return humidityAlert; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public String getObservations() { return observations; }

    // ==========================
    // BUILDER
    // ==========================
    public static class Builder {

        private UUID reportId;
        private UUID shipmentId;
        private UUID productId;
        private String origin;
        private String destination;
        private Instant dispatchTime;
        private Instant deliveryTime;
        private Double averageTemperature;
        private Double averageHumidity;
        private Boolean temperatureAlert;
        private Boolean humidityAlert;
        private String deliveryStatus;
        private String observations;

        public Builder reportId(UUID reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder shipmentId(UUID shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }

        public Builder productId(UUID productId) {
            this.productId = productId;
            return this;
        }

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder dispatchTime(Instant dispatchTime) {
            this.dispatchTime = dispatchTime;
            return this;
        }

        public Builder deliveryTime(Instant deliveryTime) {
            this.deliveryTime = deliveryTime;
            return this;
        }

        public Builder averageTemperature(Double averageTemperature) {
            this.averageTemperature = averageTemperature;
            return this;
        }

        public Builder averageHumidity(Double averageHumidity) {
            this.averageHumidity = averageHumidity;
            return this;
        }

        public Builder temperatureAlert(Boolean temperatureAlert) {
            this.temperatureAlert = temperatureAlert;
            return this;
        }

        public Builder humidityAlert(Boolean humidityAlert) {
            this.humidityAlert = humidityAlert;
            return this;
        }

        public Builder deliveryStatus(String deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public Builder observations(String observations) {
            this.observations = observations;
            return this;
        }

        public DeliveryReport build() {
            return new DeliveryReport(this);
        }
    }
}