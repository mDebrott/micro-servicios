package com.clubaereo.pasajero_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PasajeroRequest(
    @NotBlank String rut,
    @NotBlank String nombreCompleto,
    @NotNull Long jornadaId
) {}