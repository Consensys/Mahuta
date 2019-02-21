package net.consensys.mahuta.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class DefaultResponse implements Response {

    private final @Getter ResponseStatus status;
}
