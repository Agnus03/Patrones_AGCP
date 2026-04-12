package com.cadenasuministros.application.reporting.decorator;

import com.cadenasuministros.application.reporting.abstraction.DeliveryReportGenerator;
import com.cadenasuministros.domain.model.DeliveryReport;

public class LoggingReportDecorator
        extends DeliveryReportGeneratorDecorator {

    public LoggingReportDecorator(DeliveryReportGenerator delegate) {
        super(delegate);
    }

    @Override
    public DeliveryReport generate() {
        System.out.println("[LOG] Iniciando generación de reporte");
        DeliveryReport report = super.generate();
        System.out.println("[LOG] Reporte generado: " + report.getReportId());
        return report;
    }
}