package com.cadenasuministros.application.reporting.decorator;

import com.cadenasuministros.application.reporting.abstraction.DeliveryReportGenerator;
import com.cadenasuministros.domain.model.DeliveryReport;

public class ValidationReportDecorator
        extends DeliveryReportGeneratorDecorator {

    public ValidationReportDecorator(DeliveryReportGenerator delegate) {
        super(delegate);
    }

    @Override
    public DeliveryReport generate() {
        DeliveryReport report = super.generate();

        if (report.getShipmentId() == null) {
            throw new IllegalStateException("ShipmentId obligatorio");
        }

        System.out.println("[VALIDATION] Reporte válido");
        return report;
    }
}