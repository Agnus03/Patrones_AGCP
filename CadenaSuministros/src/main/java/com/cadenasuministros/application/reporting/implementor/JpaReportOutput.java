package com.cadenasuministros.application.reporting.implementor;

import com.cadenasuministros.domain.port.out.DeliveryReportRepository;
import com.cadenasuministros.domain.model.DeliveryReport;

public class JpaReportOutput implements ReportOutput {

    private final DeliveryReportRepository repository;

    public JpaReportOutput(DeliveryReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public void output(DeliveryReport report) {
        repository.save(report);
        System.out.println("Reporte guardado en BD");
    }
}