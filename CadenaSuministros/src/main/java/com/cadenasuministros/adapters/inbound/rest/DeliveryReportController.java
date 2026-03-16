package com.cadenasuministros.adapters.inbound.rest;

import com.cadenasuministros.domain.model.DeliveryReport;
import com.cadenasuministros.domain.port.in.GenerateDeliveryReportUseCase;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class DeliveryReportController {

    private final GenerateDeliveryReportUseCase useCase;

    public DeliveryReportController(GenerateDeliveryReportUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/delivery/{shipmentId}")
    public DeliveryReport generate(@PathVariable UUID shipmentId) {
        return useCase.generate(shipmentId);
    }
}
