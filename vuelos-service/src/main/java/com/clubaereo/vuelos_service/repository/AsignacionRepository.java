package com.clubaereo.vuelos_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clubaereo.vuelos_service.entities.Asignacion;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    boolean existsByPasajeroIdAndBloqueId(Long pasajeroId, Long bloqueId);

    @Query("""
           select a from Asignacion a
           join fetch a.bloque b
           where b.jornada.id = :jornadaId
           """)
    List<Asignacion> findByJornadaId(@Param("jornadaId") Long jornadaId);

}
