package net.consensys.mahuta.core.domain;

import net.consensys.mahuta.core.service.MahutaService;

public abstract class AbstractBuilder {

    protected final MahutaService service;
    
    protected AbstractBuilder(MahutaService service) {
        this.service = service;
    }
}
