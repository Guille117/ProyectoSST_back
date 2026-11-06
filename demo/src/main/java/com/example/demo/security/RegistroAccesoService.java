package com.example.demo.security;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.pacientes.paciente.PacienteRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Centraliza dos reglas compartidas por los módulos clínicos:
 * un médico solo accede a sus pacientes asignados y ningún usuario no global
 * puede editar o eliminar registros creados por otra persona.
 */
@Service
@RequiredArgsConstructor
public class RegistroAccesoService {

    private final UsuarioActualService usuarioActual;
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final EpisodioRepository episodioRepository;

    public void validarAccesoPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return;
        }
        usuarioActual.medicoRestringidoId().ifPresent(medicoId -> {
            if (!pacienteRepository.existsByIdAndMedicos_Id(pacienteId, medicoId)) {
                throw new AccessDeniedException("No tiene acceso a este paciente");
            }
        });
    }

    public void validarAccesoEpisodio(Long episodioId) {
        if (episodioId == null) {
            return;
        }
        EpisodioEntity episodio = episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Episodio no encontrado con el ID: " + episodioId));
        validarAccesoEpisodio(episodio);
    }

    public void validarAccesoEpisodio(EpisodioEntity episodio) {
        if (episodio == null || episodio.getPaciente() == null) {
            return;
        }
        validarAccesoPaciente(episodio.getPaciente().getId());
    }

    /**
     * Autor que debe quedar guardado en un registro nuevo. Se prioriza el usuario
     * autenticado para que el cliente no pueda falsear la propiedad del registro.
     */
    public UsuarioEntity autorActual(Long usuarioIdSolicitado) {
        Optional<UsuarioEntity> autenticado = usuarioActual.actual();
        if (autenticado.isPresent()) {
            return autenticado.get();
        }
        if (usuarioIdSolicitado == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        return usuarioRepository.findById(usuarioIdSolicitado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado con el ID: " + usuarioIdSolicitado));
    }

    /**
     * Autor que debe conservar una edición. Con sesión autenticada se mantiene el autor
     * original; sin autenticación (pruebas o desarrollo) se devuelve null para que el
     * servicio conserve el comportamiento anterior del contrato.
     */
    public UsuarioEntity autorParaEdicion(UsuarioEntity autorOriginal, Long usuarioIdSolicitado) {
        return usuarioActual.actual().isPresent() ? autorOriginal : null;
    }

    /** Valida que el usuario actual pueda modificar un registro con el autor indicado. */
    public void validarEdicion(UsuarioEntity autor) {
        Optional<UsuarioEntity> autenticado = usuarioActual.actual();
        if (autenticado.isEmpty() || usuarioActual.esAdministrador(autenticado.get())) {
            return;
        }
        UsuarioEntity usuario = autenticado.get();
        if (autor == null || autor.getId() == null || !autor.getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo puede modificar registros creados por usted");
        }
    }
}
