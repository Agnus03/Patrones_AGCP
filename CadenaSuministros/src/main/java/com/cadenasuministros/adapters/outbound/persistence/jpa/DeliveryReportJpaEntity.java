package com.cadenasuministros.adapters.outbound.persistence.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_reports")
public class DeliveryReportJpaEntity {

    @Id
    @Column(name = "report_id")
    private UUID reportId;

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Column(name = "product_id")
    private UUID productId;

    @Column
    private String origin;

    @Column
    private String destination;

    @Column(name = "dispatch_time")
    private Instant dispatchTime;

    @Column(name = "delivery_time")
    private Instant deliveryTime;

    @Column(name = "average_temperature")
    private Double averageTemperature;

    @Column(name = "average_humidity")
    private Double averageHumidity;

    @Column(name = "temperature_alert")
    private Boolean temperatureAlert;

    @Column(name = "humidity_alert")
    private Boolean humidityAlert;

    @Column
    private String deliveryStatus;

    @Column(columnDefinition = "TEXT")
    private String observations;

    // Constructores
    public DeliveryReportJpaEntity() {}

    // Getters/setters (o Lombok @Data si usas)
    public UUID getReportId() { return reportId; }
    public void setReportId(UUID reportId) { this.reportId = reportId; }

    public UUID getShipmentId() { return shipmentId; }
    public void setShipmentId(UUID shipmentId) { this.shipmentId = shipmentId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Instant getDispatchTime() { return dispatchTime; }
    public void setDispatchTime(Instant dispatchTime) { this.dispatchTime = dispatchTime; }

    public Instant getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(Instant deliveryTime) { this.deliveryTime = deliveryTime; }

    public Double getAverageTemperature() { return averageTemperature; }
    public void setAverageTemperature(Double averageTemperature) { this.averageTemperature = averageTemperature; }

    public Double getAverageHumidity() { return averageHumidity; }
    public void setAverageHumidity(Double averageHumidity) { this.averageHumidity = averageHumidity; }

    public Boolean getTemperatureAlert() { return temperatureAlert; }
    public void setTemperatureAlert(Boolean temperatureAlert) { this.temperatureAlert = temperatureAlert; }

    public Boolean getHumidityAlert() { return humidityAlert; }
    public void setHumidityAlert(Boolean humidityAlert) { this.humidityAlert = humidityAlert; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
