package com.clubaereo.vuelos_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.clubaereo.vuelos_service.client.AeronaveClient;
import com.clubaereo.vuelos_service.client.AeronaveExterna;
import com.clubaereo.vuelos_service.client.PasajeroClient;
import com.clubaereo.vuelos_service.client.PasajeroExterno;
import com.clubaereo.vuelos_service.client.PilotoExterno;
import com.clubaereo.vuelos_service.dto.AsignacionRequest;
import com.clubaereo.vuelos_service.dto.AsignacionResponse;
import com.clubaereo.vuelos_service.entities.Asignacion;
import com.clubaereo.vuelos_service.entities.Base;
import com.clubaereo.vuelos_service.entities.BloqueHorario;
import com.clubaereo.vuelos_service.entities.EstadoAsignacion;
import com.clubaereo.vuelos_service.entities.Jornada;
import com.clubaereo.vuelos_service.exception.ConflictoException;
import com.clubaereo.vuelos_service.repository.AsignacionRepository;
import com.clubaereo.vuelos_service.repository.BloqueHorarioRepository;
import com.clubaereo.vuelos_service.service.AsignacionService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsignacionServiceTest {

    @Mock AsignacionRepository asignacionRepo;
    @Mock BloqueHorarioRepository bloqueRepo;
    @Mock PasajeroClient pasajeroClient;
    @Mock AeronaveClient aeronaveClient;
    @InjectMocks AsignacionService service;

    private BloqueHorario bloque;
    private final AsignacionRequest req = new AsignacionRequest(1L, 1L, 1L, 1L);

    @BeforeEach
    void preparar() {
        Jornada jornada = new Jornada(1L, LocalDate.of(2026, 11, 20), Base.TOBALABA, new ArrayList<>());
        bloque = new BloqueHorario(1L, jornada, LocalTime.of(9, 0), LocalTime.of(10, 0));

        when(bloqueRepo.findById(1L)).thenReturn(Optional.of(bloque));
        when(pasajeroClient.obtener(1L)).thenReturn(
                new PasajeroExterno(1L, 1L, new BigDecimal("70.0"), "FIRMADO"));
        when(aeronaveClient.obtenerAeronave(1L)).thenReturn(
                new AeronaveExterna(1L, "CC-ABC", 3, true));
        when(aeronaveClient.obtenerPiloto(1L)).thenReturn(
                new PilotoExterno(1L, "Ana", true));
        when(asignacionRepo.save(any(Asignacion.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void crear_ok_quedaPendiente() {
        AsignacionResponse res = service.crear(req);
        assertEquals(EstadoAsignacion.PENDIENTE, res.estado());
    }

    @Test
    void crear_sinCupos_falla() {
        when(asignacionRepo.contarOcupacion(anyLong(), anyLong(), anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(3L);
        assertThrows(ConflictoException.class, () -> service.crear(req));
    }

    @Test
    void crear_pasajeroYaAsignadoEnHorarioSolapado_falla() {
        when(asignacionRepo.contarPasajeroSolapado(anyLong(), anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(1L);
        assertThrows(ConflictoException.class, () -> service.crear(req));
    }

    @Test
    void crear_aeronaveNoDisponible_falla() {
        when(aeronaveClient.obtenerAeronave(1L)).thenReturn(
                new AeronaveExterna(1L, "CC-ABC", 3, false));
        assertThrows(ConflictoException.class, () -> service.crear(req));
    }

    @Test
    void crear_pilotoSinLicencia_falla() {
        when(aeronaveClient.obtenerPiloto(1L)).thenReturn(new PilotoExterno(1L, "Ana", false));
        assertThrows(ConflictoException.class, () -> service.crear(req));
    }

    @Test
    void confirmar_sinConsentimiento_falla() {
        Asignacion a = new Asignacion(5L, 1L, 1L, 1L, bloque, EstadoAsignacion.PENDIENTE);
        when(asignacionRepo.findById(5L)).thenReturn(Optional.of(a));
        when(pasajeroClient.obtener(1L)).thenReturn(
                new PasajeroExterno(1L, 1L, new BigDecimal("70.0"), "PENDIENTE"));
        assertThrows(ConflictoException.class, () -> service.confirmar(5L));
    }

    @Test
    void confirmar_conConsentimiento_ok() {
        Asignacion a = new Asignacion(5L, 1L, 1L, 1L, bloque, EstadoAsignacion.PENDIENTE);
        when(asignacionRepo.findById(5L)).thenReturn(Optional.of(a));
        assertEquals(EstadoAsignacion.CONFIRMADA, service.confirmar(5L).estado());
    }

    @Test
    void confirmar_canceladaFalla() {
        Asignacion a = new Asignacion(5L, 1L, 1L, 1L, bloque, EstadoAsignacion.CANCELADA);
        when(asignacionRepo.findById(5L)).thenReturn(Optional.of(a));
        assertThrows(ConflictoException.class, () -> service.confirmar(5L));
    }

    @Test
    void cancelar_cambiaEstado() {
        Asignacion a = new Asignacion(5L, 1L, 1L, 1L, bloque, EstadoAsignacion.CONFIRMADA);
        when(asignacionRepo.findById(5L)).thenReturn(Optional.of(a));
        assertEquals(EstadoAsignacion.CANCELADA, service.cancelar(5L).estado());
    }
}
