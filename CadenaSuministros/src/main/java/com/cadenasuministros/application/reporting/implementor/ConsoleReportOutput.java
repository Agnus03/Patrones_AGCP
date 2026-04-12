package com.cadenasuministros.application.reporting.implementor;

import com.cadenasuministros.domain.model.DeliveryReport;

public class ConsoleReportOutput implements ReportOutput {

    @Override
    public void output(DeliveryReport report) {
        System.out.println("===== BRIDGE OUTPUT =====");
        System.out.println("Report ID: " + report.getReportId());
        System.out.println("Shipment ID: " + report.getShipmentId());
        System.out.println("Status: " + report.getDeliveryStatus());
        System.out.println("Observations: " + report.getObservations());
        System.out.println("==========================");
    }
}