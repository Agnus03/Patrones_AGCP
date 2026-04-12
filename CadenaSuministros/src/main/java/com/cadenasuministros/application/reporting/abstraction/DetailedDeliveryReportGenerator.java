package com.cadenasuministros.application.reporting.abstraction;

import com.cadenasuministros.application.reporting.implementor.ReportOutput;
import com.cadenasuministros.domain.model.DeliveryReport;

import java.time.Instant;
import java.util.UUID;

public class DetailedDeliveryReportGenerator extends DeliveryReportGenerator {

    public DetailedDeliveryReportGenerator(ReportOutput output) {
        super(output);
    }

    @Override
    public DeliveryReport generate() {

        DeliveryReport report = new DeliveryReport.Builder()
                .reportId(UUID.randomUUID())
                .shipmentId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .origin("Bodega Central")
                .destination("Sucursal Norte")
                .dispatchTime(Instant.now().minusSeconds(7200))
                .deliveryTime(Instant.now())
                .averageTemperature(6.8)
                .averageHumidity(70.0)
                .temperatureAlert(false)
                .humidityAlert(false)
                .deliveryStatus("DELIVERED")
                .observations("Reporte generado con Bridge + Builder")
                .build();

        output.output(report);   // 🔥 Bridge en acción
        return report;
    }
}