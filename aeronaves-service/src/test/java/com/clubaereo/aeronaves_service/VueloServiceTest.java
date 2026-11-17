package com.clubaereo.aeronaves_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.clubaereo.aeronaves_service.dto.CerrarVueloRequest;
import com.clubaereo.aeronaves_service.dto.VueloResponse;
import com.clubaereo.aeronaves_service.entities.Aeronave;
import com.clubaereo.aeronaves_service.entities.EstadoVuelo;
import com.clubaereo.aeronaves_service.entities.Piloto;
import com.clubaereo.aeronaves_service.entities.RegistroVuelo;
import com.clubaereo.aeronaves_service.exception.ReglaNegocioException;
import com.clubaereo.aeronaves_service.repository.AeronaveRepository;
import com.clubaereo.aeronaves_service.repository.PilotoRepository;
import com.clubaereo.aeronaves_service.repository.RegistroVueloRepository;
import com.clubaereo.aeronaves_service.service.VueloService;

@ExtendWith(MockitoExtension.class)
class VueloServiceTest {

    @Mock AeronaveRepository aeronaveRepository;
    @Mock PilotoRepository pilotoRepository;
    @Mock RegistroVueloRepository vueloRepository;
    @InjectMocks VueloService service;

    private RegistroVuelo vueloAbierto() {
        Aeronave a = new Aeronave(1L, "CC-ABC", "Cessna 172", 3, true);
        Piloto p = new Piloto(1L, "Ana", true);
        return RegistroVuelo.builder()
                .id(10L).aeronave(a).piloto(p).jornadaId(1L)
                .horometroInicial(new BigDecimal("100.0"))
                .estado(EstadoVuelo.ABIERTO).build();
    }

    @Test
    void cerrarVuelo_calculaHorasConsumidas() {
        RegistroVuelo r = vueloAbierto();
        when(vueloRepository.findByIdConDetalle(10L)).thenReturn(Optional.of(r));
        when(vueloRepository.save(any(RegistroVuelo.class))).thenAnswer(i -> i.getArgument(0));

        VueloResponse res = service.cerrarVuelo(10L,
                new CerrarVueloRequest(new BigDecimal("101.5"), 2));

        assertEquals(new BigDecimal("1.5"), res.horasConsumidas());
        assertEquals(EstadoVuelo.CERRADO, res.estado());
    }

    @Test
    void cerrarVuelo_horometroMenorOIgual_falla() {
        when(vueloRepository.findByIdConDetalle(10L)).thenReturn(Optional.of(vueloAbierto()));
        assertThrows(ReglaNegocioException.class, () -> service.cerrarVuelo(10L,
                new CerrarVueloRequest(new BigDecimal("100.0"), 1)));
    }

    @Test
    void cerrarVuelo_tripulantesSobreCapacidad_falla() {
        when(vueloRepository.findByIdConDetalle(10L)).thenReturn(Optional.of(vueloAbierto()));
        assertThrows(ReglaNegocioException.class, () -> service.cerrarVuelo(10L,
                new CerrarVueloRequest(new BigDecimal("101.0"), 4)));
    }

    @Test
    void cerrarVuelo_yaCerrado_falla() {
        RegistroVuelo r = vueloAbierto();
        r.setEstado(EstadoVuelo.CERRADO);
        when(vueloRepository.findByIdConDetalle(10L)).thenReturn(Optional.of(r));
        assertThrows(ReglaNegocioException.class, () -> service.cerrarVuelo(10L,
                new CerrarVueloRequest(new BigDecimal("101.0"), 1)));
    }
}
