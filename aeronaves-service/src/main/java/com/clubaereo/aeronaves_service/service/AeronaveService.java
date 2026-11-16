package com.clubaereo.aeronaves_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clubaereo.aeronaves_service.dto.AeronaveResponse;
import com.clubaereo.aeronaves_service.repository.AeronaveRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AeronaveService {
    private final AeronaveRepository aeronaveRepository;

    @Transactional
    public List<AeronaveResponse> listarDisponibles() {
        return aeronaveRepository.findByDisponibleTrue()
                .stream()
                .map(AeronaveResponse::from)
                .toList();
    }
}
