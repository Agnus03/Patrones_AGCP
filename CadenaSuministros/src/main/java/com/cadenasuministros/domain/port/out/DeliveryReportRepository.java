package com.cadenasuministros.domain.port.out;

import com.cadenasuministros.domain.model.DeliveryReport;

public interface DeliveryReportRepository {
    DeliveryReport save(DeliveryReport report);
}
