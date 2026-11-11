package com.clubaereo.pasajero_service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PesoRequest(
    @NotNull @DecimalMin("20.0") @DecimalMax("250.0") Double pesoKg
) {}
