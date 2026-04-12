package com.cadenasuministros.application.reporting.decorator;

import com.cadenasuministros.application.reporting.abstraction.DeliveryReportGenerator;
import com.cadenasuministros.domain.model.DeliveryReport;

public abstract class DeliveryReportGeneratorDecorator
        extends DeliveryReportGenerator {

    protected final DeliveryReportGenerator delegate;

    protected DeliveryReportGeneratorDecorator(
            DeliveryReportGenerator delegate) {
        super(delegate.output);
        this.delegate = delegate;
    }

    @Override
    public DeliveryReport generate() {
        return delegate.generate();
    }
}