package com.clubaereo.vuelos_service.service;

import org.springframework.stereotype.Service;

import com.clubaereo.vuelos_service.dto.AsignacionRequest;
import com.clubaereo.vuelos_service.dto.AsignacionResponse;
import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;
import com.clubaereo.vuelos_service.exception.ConflictoException;
import com.clubaereo.vuelos_service.exception.RecursoNoEncontradoException;
import com.clubaereo.vuelos_service.repository.AsignacionRepository;
import com.clubaereo.vuelos_service.repository.BloqueHorarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsignacionService {

    private final AsignacionRepository asignacionRepo;
    private final BloqueHorarioRepository bloqueRepo;

    @Transactional
    public AsignacionResponse crear(AsignacionRequest req) {
        BloqueHorario bloque = buscarBloque(req.bloqueId());

        if (asignacionRepo.existsByPasajeroIdAndBloqueId(req.pasajeroId(), req.bloqueId())) {
            throw new ConflictoException("El pasajero ya está asignado a este bloque");
        }

        Asignacion a = new Asignacion();
        a.setPasajeroId(req.pasajeroId());
        a.setAeronaveId(req.aeronaveId());
        a.setPilotoId(req.pilotoId());
        a.setBloque(bloque);
        // estado queda en PENDIENTE por defecto

        return AsignacionResponse.from(asignacionRepo.save(a));
    }

    @Transactional
    public AsignacionResponse actualizar(Long id, AsignacionRequest req) {
        Asignacion a = buscarAsignacion(id);

        if (a.getEstado() != EstadoAsignacion.PENDIENTE) {
            throw new ConflictoException("Solo se pueden reasignar asignaciones PENDIENTES");
        }

        boolean cambiaBloque = !a.getBloque().getId().equals(req.bloqueId());
        if (cambiaBloque) {
            if (asignacionRepo.existsByPasajeroIdAndBloqueId(req.pasajeroId(), req.bloqueId())) {
                throw new ConflictoException("El pasajero ya está asignado al bloque destino");
            }
            a.setBloque(buscarBloque(req.bloqueId()));
        }

        a.setPasajeroId(req.pasajeroId());
        a.setAeronaveId(req.aeronaveId());
        a.setPilotoId(req.pilotoId());

        return AsignacionResponse.from(a); // @Transactional persiste el cambio solo
    }

    @Transactional
    public AsignacionResponse confirmar(Long id) {
        Asignacion a = buscarAsignacion(id);

        if (a.getEstado() == EstadoAsignacion.CANCELADA) {
            throw new ConflictoException("No se puede confirmar una asignación cancelada");
        }
        // Si ya estaba CONFIRMADA, no hacemos nada: operación idempotente
        a.setEstado(EstadoAsignacion.CONFIRMADA);
        return AsignacionResponse.from(a);
    }

    private BloqueHorario buscarBloque(Long id) {
        return bloqueRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Bloque " + id + " no existe"));
    }

    private Asignacion buscarAsignacion(Long id) {
        return asignacionRepo.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Asignación " + id + " no existe"));
    }
}
