package com.example.demo.modules.pacientes.expediente;

import com.example.demo.modules.clinica.alta.EstadoAlta;
import com.example.demo.modules.clinica.alta.altaEntity;
import com.example.demo.modules.clinica.alta.altaRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.PacienteRepository;
import com.example.demo.modules.pacientes.paciente.TipoAtencion;
import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpedienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;
    @Mock
    private EpisodioRepository episodioRepository;
    @Mock
    private altaRepository altaRepository;

    @InjectMocks
    private ExpedienteService service;

    @Test
    void listarEpisodios_devuelveElUltimoEpisodioConSuEgresoYMedico() {
        UsuarioEntity medico = new UsuarioEntity();
        PersonaEntity persona = new PersonaEntity();
        persona.setPrimerNombre("Ana");
        persona.setPrimerApellido("Pérez");
        medico.setPersona(persona);

        LocalDateTime ingreso = LocalDateTime.of(2026, 10, 20, 8, 0);
        LocalDateTime egreso = LocalDateTime.of(2026, 10, 22, 9, 0);
        EpisodioEntity episodio = EpisodioEntity.builder()
                .id(7L)
                .tipoAtencion(TipoAtencion.HOSPITALIZACION)
                .fechaHoraIngreso(ingreso)
                .medico(medico)
                .estado(false)
                .build();

        when(pacienteRepository.existsById(3L)).thenReturn(true);
        when(episodioRepository.findByPaciente_IdOrderByIdDesc(3L))
                .thenReturn(List.of(episodio));
        when(altaRepository.findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                7L, EstadoAlta.VIGENTE))
                .thenReturn(Optional.of(altaEntity.builder().fechaHoraEgreso(egreso).build()));

        List<ExpedienteDTOs.EpisodioResumenResponse> respuesta = service.listarEpisodios(3L);

        assertEquals(1, respuesta.size());
        ExpedienteDTOs.EpisodioResumenResponse item = respuesta.get(0);
        assertEquals(7L, item.episodioId());
        assertEquals(TipoAtencion.HOSPITALIZACION, item.tipoAtencion());
        assertEquals(ingreso, item.fechaHoraIngreso());
        assertEquals(egreso, item.fechaHoraEgreso());
        assertEquals("Ana Pérez", item.medicoNombre());
        assertFalse(item.activo());
    }

    @Test
    void listarEpisodios_rechazaPacienteInexistente() {
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.listarEpisodios(99L));
    }
}
