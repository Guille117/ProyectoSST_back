package com.example.demo.modules.clinica.indicaciones.medicamentos;

import com.example.demo.modules.clinica.indicaciones.DuracionValidator;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogEntity;
import com.example.demo.modules.farmacia.medicamentoLog.medicamentoLogRepository;
import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class indicacionMedicamentoService {

    private final indicacionMedicamentoRepository repository;
    private final medicamentoLogRepository medicamentoLogRepository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public indicacionMedicamentoDTOs.Response crear(indicacionMedicamentoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        medicamentoLogEntity medicamento = resolverMedicamentoLog(request.medicamentoLogId());
        String medicamentoLibre = resolverMedicamentoLibre(request);

        indicacionMedicamentoEntity entity = indicacionMedicamentoEntity.builder()
                .medicamentoLog(medicamento)
                .medicamentoLibre(medicamentoLibre)
                .presentacion(StringNormalizer.normalizarNullable(request.presentacion()))
                .cantidad(validarCantidad(request.cantidad()))
                .frecuencia(validarFrecuencia(request.frecuencia()))
                .duracion(DuracionValidator.validar(request.tipoDuracion(), request.duracion()))
                .tipoDuracion(request.tipoDuracion())
                .observaciones(StringNormalizer.normalizarNullable(request.observaciones()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .fechaInicio(validarFechaInicio(request.fechaInicio()))
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public indicacionMedicamentoDTOs.Response actualizar(Long id, indicacionMedicamentoDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        indicacionMedicamentoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));

        entity.setMedicamentoLog(resolverMedicamentoLog(request.medicamentoLogId()));
        entity.setMedicamentoLibre(resolverMedicamentoLibre(request));
        entity.setPresentacion(StringNormalizer.normalizarNullable(request.presentacion()));
        entity.setCantidad(validarCantidad(request.cantidad()));
        entity.setFrecuencia(validarFrecuencia(request.frecuencia()));
        entity.setDuracion(DuracionValidator.validar(request.tipoDuracion(), request.duracion()));
        entity.setTipoDuracion(request.tipoDuracion());
        entity.setObservaciones(StringNormalizer.normalizarNullable(request.observaciones()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));
        entity.setFechaInicio(validarFechaInicio(request.fechaInicio()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public indicacionMedicamentoDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<indicacionMedicamentoDTOs.Response> obtenerTodos(EstadoIndicacionMedicamento estado) {
        return repository.findByEstadoOrderByFechaDesc(validarEstado(estado)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<indicacionMedicamentoDTOs.Response> buscarPorEpisodio(Long episodioId, EstadoIndicacionMedicamento estado) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(episodioId, validarEstado(estado)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public indicacionMedicamentoDTOs.Response cambiarEstado(Long id, EstadoIndicacionMedicamento estado) {
        indicacionMedicamentoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));

        entity.setEstado(validarEstado(estado));
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void eliminar(Long id) {
        indicacionMedicamentoEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));

        entity.setEstado(EstadoIndicacionMedicamento.ELIMINADO);
        repository.save(entity);
    }

    private EstadoIndicacionMedicamento validarEstado(EstadoIndicacionMedicamento estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        return estado;
    }

    private Integer validarCantidad(Integer cantidad) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad es obligatoria");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        return cantidad;
    }

    private Integer validarFrecuencia(Integer frecuencia) {
        if (frecuencia == null) {
            throw new IllegalArgumentException("La frecuencia es obligatoria");
        }
        if (frecuencia <= 0) {
            throw new IllegalArgumentException("La frecuencia debe ser mayor a 0");
        }
        return frecuencia;
    }

    private LocalDateTime validarFechaInicio(LocalDateTime fechaInicio) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha y hora de inicio son obligatorias");
        }
        return fechaInicio;
    }

    /** El medicamento puede venir del catálogo o escribirse libremente si no está registrado. */
    private medicamentoLogEntity resolverMedicamentoLog(Long medicamentoLogId) {
        return medicamentoLogId == null ? null : buscarMedicamento(medicamentoLogId);
    }

    private String resolverMedicamentoLibre(indicacionMedicamentoDTOs.Request request) {
        if (request.medicamentoLogId() != null) {
            return null;
        }

        String libre = StringNormalizer.normalizarNullable(request.medicamentoLibre());
        if (libre == null) {
            throw new IllegalArgumentException("El medicamento es obligatorio");
        }
        return libre;
    }

    private medicamentoLogEntity buscarMedicamento(Long medicamentoLogId) {
        return medicamentoLogRepository.findById(medicamentoLogId)
                .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado con el ID: " + medicamentoLogId));
    }

    private EpisodioEntity buscarEpisodio(Long episodioId) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }

        return episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException("Episodio no encontrado con el ID: " + episodioId));
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

    private indicacionMedicamentoDTOs.Response toResponse(indicacionMedicamentoEntity entity) {
        Long medicamentoLogId = entity.getMedicamentoLog() == null
                ? null : entity.getMedicamentoLog().getId();
        String medicamento = entity.getMedicamentoLog() == null
                ? entity.getMedicamentoLibre() : entity.getMedicamentoLog().getNombre();

        return new indicacionMedicamentoDTOs.Response(
                entity.getId(),
                medicamentoLogId,
                medicamento,
                entity.getMedicamentoLibre(),
                entity.getPresentacion(),
                entity.getCantidad(),
                entity.getFrecuencia(),
                entity.getDuracion(),
                entity.getTipoDuracion(),
                entity.getObservaciones(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.getEstado(),
                entity.getFechaInicio());
    }
}
