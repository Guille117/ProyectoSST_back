package com.example.demo.modules.enfermeria.tratamientos.ejecucionesCuidado;

import com.example.demo.modules.clinica.indicaciones.cuidados.EstadoCuidado;
import com.example.demo.modules.clinica.indicaciones.cuidados.cuidadoEntity;
import com.example.demo.modules.clinica.indicaciones.cuidados.cuidadoRepository;
import com.example.demo.modules.enfermeria.monitoreo.controlHidrico.controlHidricoEntity;
import com.example.demo.modules.enfermeria.monitoreo.controlHidrico.controlHidricoRepository;
import com.example.demo.modules.enfermeria.monitoreo.glucosa.glucosaEntity;
import com.example.demo.modules.enfermeria.monitoreo.glucosa.glucosaRepository;
import com.example.demo.modules.enfermeria.monitoreo.signosVitales.signosVitalesEntity;
import com.example.demo.modules.enfermeria.monitoreo.signosVitales.signosVitalesRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ejecucionCuidadoService {

    private final ejecucionCuidadoRepository repository;
    private final cuidadoRepository cuidadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final signosVitalesRepository signosVitalesRepository;
    private final glucosaRepository glucosaRepository;
    private final controlHidricoRepository controlHidricoRepository;

    @Transactional
    public ejecucionCuidadoDTOs.Response crear(ejecucionCuidadoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        cuidadoEntity cuidado = buscarCuidado(request.cuidadoId());
        if (cuidado.getEstado() == EstadoCuidado.ELIMINADO) {
            throw new IllegalArgumentException("El cuidado está eliminado");
        }
        if (request.fechaHoraProgramada() == null) {
            throw new IllegalArgumentException("La fecha y hora programada son obligatorias");
        }
        if (repository.existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalse(
                cuidado.getId(), request.fechaHoraProgramada())) {
            throw new IllegalArgumentException("Ya existe un registro para esa ejecución");
        }

        ejecucionCuidadoEntity entity = ejecucionCuidadoEntity.builder()
                .cuidado(cuidado)
                .fechaHoraProgramada(request.fechaHoraProgramada())
                .estado(validarEstado(request.estado()))
                .observacion(StringNormalizer.normalizarNullable(request.observacion()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fechaRegistro(LocalDateTime.now())
                .build();

        asignarMonitoreo(entity, request, cuidado);

        return toResponse(repository.save(entity));
    }

    @Transactional
    public ejecucionCuidadoDTOs.Response actualizar(Long id, ejecucionCuidadoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        ejecucionCuidadoEntity entity = buscar(id);

        validarFechaProgramadaParaActualizar(entity, request.fechaHoraProgramada());

        entity.setFechaHoraProgramada(request.fechaHoraProgramada());
        entity.setEstado(validarEstado(request.estado()));
        entity.setObservacion(StringNormalizer.normalizarNullable(request.observacion()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ejecucionCuidadoDTOs.Response obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ejecucionCuidadoDTOs.Response> buscar(Long cuidadoId, Long episodioId) {
        if (cuidadoId != null && episodioId != null) {
            throw new IllegalArgumentException("Indique el cuidado o el episodio, no ambos");
        }
        if (cuidadoId != null) {
            return buscarPorCuidado(cuidadoId);
        }
        if (episodioId != null) {
            return buscarPorEpisodio(episodioId);
        }
        throw new IllegalArgumentException("Debe indicar el cuidado o el episodio");
    }

    @Transactional(readOnly = true)
    public List<ejecucionCuidadoDTOs.Response> buscarPorCuidado(Long cuidadoId) {
        return repository.findByCuidado_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(cuidadoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ejecucionCuidadoDTOs.Response> buscarPorEpisodio(Long episodioId) {
        return repository.findByCuidado_Episodio_IdAndEliminadoFalseOrderByFechaHoraProgramadaAsc(episodioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        ejecucionCuidadoEntity entity = buscar(id);
        entity.setEliminado(true);
        repository.save(entity);
    }

    private void asignarMonitoreo(
            ejecucionCuidadoEntity entity,
            ejecucionCuidadoDTOs.Request request,
            cuidadoEntity cuidado) {
        int referencias = 0;
        if (request.signosVitalesId() != null) referencias++;
        if (request.glucosaId() != null) referencias++;
        if (request.controlHidricoId() != null) referencias++;

        if (referencias > 1) {
            throw new IllegalArgumentException("Una ejecución solo puede vincular un registro de monitoreo");
        }
        if (referencias == 1 && entity.getEstado() != EstadoEjecucionCuidado.REALIZADO) {
            throw new IllegalArgumentException("Un control vinculado debe estar realizado");
        }

        if (request.signosVitalesId() != null) {
            signosVitalesEntity signos = buscarSignosVitales(request.signosVitalesId());
            validarEpisodio(cuidado, signos.getEpisodio().getId(), "signos vitales");
            entity.setSignosVitales(signos);
        }

        if (request.glucosaId() != null) {
            glucosaEntity glucosa = buscarGlucosa(request.glucosaId());
            validarEpisodio(cuidado, glucosa.getEpisodio().getId(), "glucosa");
            entity.setGlucosa(glucosa);
        }

        if (request.controlHidricoId() != null) {
            controlHidricoEntity hidrico = buscarControlHidrico(request.controlHidricoId());
            validarEpisodio(cuidado, hidrico.getEpisodio().getId(), "control hídrico");
            entity.setControlHidrico(hidrico);
        }
    }

    private signosVitalesEntity buscarSignosVitales(Long id) {
        signosVitalesEntity entity = signosVitalesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registro de signos vitales no encontrado con el ID: " + id));
        if (entity.isEliminado()) {
            throw new IllegalArgumentException("El registro de signos vitales está eliminado");
        }
        return entity;
    }

    private glucosaEntity buscarGlucosa(Long id) {
        glucosaEntity entity = glucosaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registro de glucosa no encontrado con el ID: " + id));
        if (entity.isEliminado()) {
            throw new IllegalArgumentException("El registro de glucosa está eliminado");
        }
        return entity;
    }

    private controlHidricoEntity buscarControlHidrico(Long id) {
        controlHidricoEntity entity = controlHidricoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registro de control hídrico no encontrado con el ID: " + id));
        if (entity.isEliminado()) {
            throw new IllegalArgumentException("El registro de control hídrico está eliminado");
        }
        return entity;
    }

    private void validarEpisodio(cuidadoEntity cuidado, Long episodioId, String tipo) {
        if (!Objects.equals(cuidado.getEpisodio().getId(), episodioId)) {
            throw new IllegalArgumentException(
                    "El registro de " + tipo + " no pertenece al episodio del cuidado");
        }
    }

    private ejecucionCuidadoEntity buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejecución de cuidado no encontrada con el ID: " + id));
    }

    private cuidadoEntity buscarCuidado(Long cuidadoId) {
        if (cuidadoId == null) {
            throw new IllegalArgumentException("El cuidado es obligatorio");
        }
        return cuidadoRepository.findById(cuidadoId)
                .orElseThrow(() -> new IllegalArgumentException("Cuidado no encontrado con el ID: " + cuidadoId));
    }

    private EstadoEjecucionCuidado validarEstado(EstadoEjecucionCuidado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        return estado;
    }

    /** Al editar se valida que la nueva fecha programada no duplique otro registro vigente. */
    private void validarFechaProgramadaParaActualizar(
            ejecucionCuidadoEntity entity, LocalDateTime fechaHoraProgramada) {
        if (fechaHoraProgramada == null) {
            throw new IllegalArgumentException("La fecha y hora programada son obligatorias");
        }
        if (repository.existsByCuidado_IdAndFechaHoraProgramadaAndEliminadoFalseAndIdNot(
                entity.getCuidado().getId(), fechaHoraProgramada, entity.getId())) {
            throw new IllegalArgumentException("Ya existe un registro para esa ejecución");
        }
    }

    private UsuarioEntity buscarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el ID: " + usuarioId));
    }

    private String nombreUsuario(UsuarioEntity usuario) {
        if (usuario == null) {
            return null;
        }

        String nombreCompleto = usuario.getPersona() != null ? usuario.getPersona().getNombreCompleto() : "";
        return nombreCompleto.isBlank() ? usuario.getUsername() : nombreCompleto;
    }

    private ejecucionCuidadoDTOs.Response toResponse(ejecucionCuidadoEntity entity) {
        return new ejecucionCuidadoDTOs.Response(
                entity.getId(),
                entity.getCuidado().getId(),
                entity.getFechaHoraProgramada(),
                entity.getEstado(),
                entity.getObservacion(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFechaRegistro(),
                entity.getSignosVitales() != null ? entity.getSignosVitales().getId() : null,
                entity.getGlucosa() != null ? entity.getGlucosa().getId() : null,
                entity.getControlHidrico() != null ? entity.getControlHidrico().getId() : null);
    }
}
