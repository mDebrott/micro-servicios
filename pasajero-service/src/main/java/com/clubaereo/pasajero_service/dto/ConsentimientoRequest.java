package com.clubaereo.pasajero_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsentimientoRequest(
    @NotBlank String firmaBase64
) {}