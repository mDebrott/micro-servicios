package com.clubaereo.aeronaves_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubaereo.aeronaves_service.dto.AeronaveResponse;
import com.clubaereo.aeronaves_service.service.AeronaveService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/aeronaves")
@RequiredArgsConstructor 
public class AeronaveController {

    private final AeronaveService aeronaveService;

    @GetMapping 
    public List<AeronaveResponse> listarDisponibles() {
        return aeronaveService.listarDisponibles();
    }
}