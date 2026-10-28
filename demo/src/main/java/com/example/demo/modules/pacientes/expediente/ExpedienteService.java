package com.example.demo.modules.pacientes.expediente;

import com.example.demo.modules.clinica.alta.EstadoAlta;
import com.example.demo.modules.clinica.alta.altaEntity;
import com.example.demo.modules.clinica.alta.altaRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpedienteService {

    private final PacienteRepository pacienteRepository;
    private final EpisodioRepository episodioRepository;
    private final altaRepository altaRepository;

    /** Episodios del paciente, del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<ExpedienteDTOs.EpisodioResumenResponse> listarEpisodios(Long pacienteId) {
        if (pacienteId == null) {
            throw new IllegalArgumentException("El paciente es obligatorio");
        }
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new IllegalArgumentException(
                    "Paciente no encontrado con el ID: " + pacienteId);
        }

        return episodioRepository.findByPaciente_IdOrderByIdDesc(pacienteId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ExpedienteDTOs.EpisodioResumenResponse toResponse(EpisodioEntity episodio) {
        LocalDateTime fechaHoraEgreso = altaRepository
                .findFirstByEpisodio_IdAndEstadoOrderByFechaHoraEgresoDesc(
                        episodio.getId(), EstadoAlta.VIGENTE)
                .map(altaEntity::getFechaHoraEgreso)
                .orElse(null);

        String medicoNombre = episodio.getMedico() != null
                && episodio.getMedico().getPersona() != null
                ? episodio.getMedico().getPersona().getNombreCompleto()
                : null;

        return new ExpedienteDTOs.EpisodioResumenResponse(
                episodio.getId(),
                episodio.getTipoAtencion(),
                episodio.getFechaHoraIngreso(),
                fechaHoraEgreso,
                medicoNombre,
                episodio.isEstado());
    }
}
