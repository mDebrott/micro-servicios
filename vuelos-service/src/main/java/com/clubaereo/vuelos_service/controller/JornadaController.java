package com.clubaereo.vuelos_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubaereo.vuelos_service.dto.BloqueResponse;
import com.clubaereo.vuelos_service.service.JornadaService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/jornadas")
@RequiredArgsConstructor 
public class JornadaController {

    private final JornadaService service;

    @GetMapping ("/{id}/bloques")
    public List<BloqueResponse> bloques(@PathVariable Long id) {
        return service.obtenerBloques(id);
    }
}