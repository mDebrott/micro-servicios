package com.clubaereo.vuelos_service.dto;

import java.util.Map;

public record ErrorResponse(
    int status,
    String mensaje,
    Map<String, String> errores
) {

}
