package com.example.demo.modules.pacientes.area;

import com.example.demo.modules.pacientes.camas.camasRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class areaServiceTest {

    @Mock
    private areaRepository repository;

    @Mock
    private camasRepository camasRepository;

    @InjectMocks
    private areaService service;

    @Test
    void actualizarConDescripcion_debeRechazarAreaRelacionadaConCamas() {
        areaEntity area = new areaEntity();
        area.setId(1L);
        area.setNombre("Emergencia");
        area.setDescripcion("Área de emergencia");
        areaEntity detalles = new areaEntity();
        detalles.setNombre("Urgencias");
        detalles.setDescripcion("Área de urgencias");
        when(repository.findById(1L)).thenReturn(Optional.of(area));
        when(camasRepository.existsByArea_Id(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.actualizarConDescripcion(1L, detalles));
        verify(repository, never()).save(area);
    }

    @Test
    void cambiarEstado_debeRechazarDesactivarAreaRelacionadaConCamas() {
        areaEntity area = new areaEntity();
        area.setId(1L);
        area.setNombre("Emergencia");
        area.setEstado(true);
        when(repository.findById(1L)).thenReturn(Optional.of(area));
        when(camasRepository.existsByArea_Id(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstado(1L));
        verify(repository, never()).save(area);
    }
}