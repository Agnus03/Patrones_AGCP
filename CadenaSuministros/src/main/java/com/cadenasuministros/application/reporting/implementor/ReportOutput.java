package com.cadenasuministros.application.reporting.implementor;

import com.cadenasuministros.domain.model.DeliveryReport;

public interface ReportOutput {
    void output(DeliveryReport report);
}