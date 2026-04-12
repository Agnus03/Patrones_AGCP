package com.cadenasuministros.application.reporting.abstraction;

import com.cadenasuministros.application.reporting.implementor.ReportOutput;
import com.cadenasuministros.domain.model.DeliveryReport;

public abstract class DeliveryReportGenerator {

    public ReportOutput output;

    protected DeliveryReportGenerator(ReportOutput output) {
        this.output = output;
    }

    public abstract DeliveryReport generate();
}