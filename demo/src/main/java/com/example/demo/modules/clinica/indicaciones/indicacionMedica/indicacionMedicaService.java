package com.example.demo.modules.clinica.indicaciones.indicacionMedica;

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
public class indicacionMedicaService {

    private final indicacionMedicaRepository repository;
    private final medicamentoLogRepository medicamentoLogRepository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public indicacionMedicaDTOs.Response crear(indicacionMedicaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        indicacionMedicaEntity entity = indicacionMedicaEntity.builder()
                .medicamentoLog(buscarMedicamento(request.medicamentoLogId()))
                .cantidad(validarCantidad(request.cantidad()))
                .frecuencia(validarFrecuencia(request.frecuencia()))
                .duracion(DuracionValidator.validar(request.tipoDuracion(), request.duracion()))
                .tipoDuracion(request.tipoDuracion())
                .observaciones(StringNormalizer.normalizarNullable(request.observaciones()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Transactional
    public indicacionMedicaDTOs.Response actualizar(Long id, indicacionMedicaDTOs.Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        indicacionMedicaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));

        entity.setMedicamentoLog(buscarMedicamento(request.medicamentoLogId()));
        entity.setCantidad(validarCantidad(request.cantidad()));
        entity.setFrecuencia(validarFrecuencia(request.frecuencia()));
        entity.setDuracion(DuracionValidator.validar(request.tipoDuracion(), request.duracion()));
        entity.setTipoDuracion(request.tipoDuracion());
        entity.setObservaciones(StringNormalizer.normalizarNullable(request.observaciones()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public indicacionMedicaDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<indicacionMedicaDTOs.Response> obtenerTodos(EstadoIndicacionMedica estado) {
        return repository.findByEstadoOrderByFechaDesc(validarEstado(estado)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<indicacionMedicaDTOs.Response> buscarPorEpisodio(Long episodioId, EstadoIndicacionMedica estado) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(episodioId, validarEstado(estado)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public indicacionMedicaDTOs.Response cambiarEstado(Long id, EstadoIndicacionMedica estado) {
        indicacionMedicaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));

        entity.setEstado(validarEstado(estado));
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void eliminar(Long id) {
        indicacionMedicaEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicación médica no encontrada con el ID: " + id));

        entity.setEstado(EstadoIndicacionMedica.ELIMINADO);
        repository.save(entity);
    }

    private EstadoIndicacionMedica validarEstado(EstadoIndicacionMedica estado) {
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

    private medicamentoLogEntity buscarMedicamento(Long medicamentoLogId) {
        if (medicamentoLogId == null) {
            throw new IllegalArgumentException("El medicamento es obligatorio");
        }

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

    private indicacionMedicaDTOs.Response toResponse(indicacionMedicaEntity entity) {
        return new indicacionMedicaDTOs.Response(
                entity.getId(),
                entity.getMedicamentoLog().getId(),
                entity.getMedicamentoLog().getNombre(),
                entity.getCantidad(),
                entity.getFrecuencia(),
                entity.getDuracion(),
                entity.getTipoDuracion(),
                entity.getObservaciones(),
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.getEstado());
    }
}
