package com.clubaereo.vuelos_service.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.clubaereo.vuelos_service.dto.AsignacionResponse;
import com.clubaereo.vuelos_service.dto.BloqueResponse;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.repository.AsignacionRepository;
import com.clubaereo.vuelos_service.repository.BloqueHorarioRepository;
import com.clubaereo.vuelos_service.repository.JornadaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class JornadaService {

    private final JornadaRepository jornadaRepo;
    private final BloqueHorarioRepository bloqueRepo;
    private final AsignacionRepository asignacionRepo;

    @Transactional
    public List<BloqueResponse> obtenerBloques(Long jornadaId) {
        if (!jornadaRepo.existsById(jornadaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Jornada no existe");
        }

        // Consulta 1: bloques
        List<BloqueHorario> bloques = bloqueRepo.findByJornadaIdOrderByHoraInicio(jornadaId);

        // Consulta 2: asignaciones, agrupadas por bloque
        Map<Long, List<AsignacionResponse>> porBloque = asignacionRepo.findByJornadaId(jornadaId)
            .stream()
            .map(AsignacionResponse::from)
            .collect(Collectors.groupingBy(AsignacionResponse::bloqueId));

        return bloques.stream()
            .map(b -> new BloqueResponse(
                b.getId(), b.getHoraInicio(), b.getHoraFin(),
                porBloque.getOrDefault(b.getId(), List.of())))
            .toList();
    }
}
