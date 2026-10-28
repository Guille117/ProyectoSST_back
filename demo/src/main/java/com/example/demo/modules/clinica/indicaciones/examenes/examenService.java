package com.example.demo.modules.clinica.indicaciones.examenes;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import com.example.demo.utils.StringNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class examenService {

    private final examenRepository repository;
    private final examenArchivoRepository archivoRepository;
    private final ExamenArchivoStorage archivoStorage;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public examenDTOs.Response crear(examenDTOs.Request request, List<MultipartFile> archivos) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        examenEntity entity = examenEntity.builder()
                .nombreExamen(validarNombre(request.nombreExamen()))
                .observacion(StringNormalizer.normalizarNullable(request.observacion()))
                .episodio(buscarEpisodio(request.episodioId()))
                .usuario(buscarUsuario(request.usuarioId()))
                .fecha(LocalDateTime.now())
                .build();

        examenEntity guardado = repository.save(entity);

        List<String> rutasGuardadas = new ArrayList<>();
        try {
            guardarArchivos(guardado, archivos, rutasGuardadas);
            return toResponse(repository.save(guardado));
        } catch (RuntimeException ex) {
            eliminarArchivosSilencioso(rutasGuardadas, ex);
            throw ex;
        }
    }

    @Transactional
    public examenDTOs.Response actualizar(Long id, examenDTOs.Request request, List<MultipartFile> archivos) {
        if (request == null) {
            throw new IllegalArgumentException("Sin datos para actualizar");
        }

        examenEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con el ID: " + id));

        entity.setNombreExamen(validarNombre(request.nombreExamen()));
        entity.setObservacion(StringNormalizer.normalizarNullable(request.observacion()));
        entity.setEpisodio(buscarEpisodio(request.episodioId()));
        entity.setUsuario(buscarUsuario(request.usuarioId()));

        List<String> rutasGuardadas = new ArrayList<>();
        try {
            guardarArchivos(entity, archivos, rutasGuardadas);
            return toResponse(repository.save(entity));
        } catch (RuntimeException ex) {
            eliminarArchivosSilencioso(rutasGuardadas, ex);
            throw ex;
        }
    }

    @Transactional
    public examenDTOs.Response agregarArchivos(Long id, List<MultipartFile> archivos) {
        examenEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con el ID: " + id));

        List<String> rutasGuardadas = new ArrayList<>();
        try {
            guardarArchivos(entity, archivos, rutasGuardadas);
            return toResponse(repository.save(entity));
        } catch (RuntimeException ex) {
            eliminarArchivosSilencioso(rutasGuardadas, ex);
            throw ex;
        }
    }

    @Transactional
    public void eliminarArchivo(Long examenId, Long archivoId) {
        examenEntity entity = repository.findById(examenId)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con el ID: " + examenId));

        examenArchivoEntity objetivo = entity.getArchivos().stream()
                .filter(archivo -> archivoId.equals(archivo.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Archivo del examen no encontrado con el ID: " + archivoId));

        entity.getArchivos().remove(objetivo);
        repository.save(entity);
        archivoStorage.eliminar(objetivo.getUrlArchivo());
    }

    @Transactional(readOnly = true)
    public ExamenArchivoStorage.Archivo cargarArchivo(Long examenId, Long archivoId) {
        examenEntity entity = repository.findById(examenId)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con el ID: " + examenId));

        examenArchivoEntity archivo = entity.getArchivos().stream()
                .filter(item -> archivoId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Archivo del examen no encontrado con el ID: " + archivoId));

        return archivoStorage.cargar(archivo.getUrlArchivo());
    }

    @Transactional(readOnly = true)
    public examenDTOs.Response obtenerPorId(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con el ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<examenDTOs.Response> obtenerTodos(EstadoExamen estado) {
        return repository.findByEstadoOrderByFechaDesc(validarEstado(estado)).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<examenDTOs.Response> buscarPorEpisodio(Long episodioId, EstadoExamen estado) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio para la búsqueda");
        }

        return repository.findByEpisodio_IdAndEstadoOrderByFechaDesc(episodioId, validarEstado(estado)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public examenDTOs.Response cambiarEstado(Long id, EstadoExamen estado) {
        examenEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con el ID: " + id));

        entity.setEstado(validarEstado(estado));
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void eliminar(Long id) {
        examenEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con el ID: " + id));

        entity.setEstado(EstadoExamen.ELIMINADO);
        repository.save(entity);
    }

    private void guardarArchivos(examenEntity examen, List<MultipartFile> archivos, List<String> rutasGuardadas) {
        if (archivos == null || archivos.isEmpty()) {
            return;
        }

        for (MultipartFile archivo : archivos) {
            if (archivo == null) {
                continue;
            }
            String ruta = archivoStorage.guardar(archivo, examen.getId());
            rutasGuardadas.add(ruta);
            examenArchivoEntity archivoEntity = examenArchivoEntity.builder()
                    .examen(examen)
                    .nombreOriginal(nombreOriginal(archivo))
                    .urlArchivo(ruta)
                    .tamano(archivo.getSize())
                    .build();
            archivoRepository.save(archivoEntity);
            examen.getArchivos().add(archivoEntity);
        }
    }

    private String nombreOriginal(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.isBlank()) {
            nombre = "archivo";
        }
        nombre = nombre.trim();
        return nombre.length() > 255 ? nombre.substring(0, 255) : nombre;
    }

    private String nombreOriginal(examenArchivoEntity archivo) {
        String nombre = archivo.getNombreOriginal();
        if (nombre != null && !nombre.isBlank()) {
            return nombre;
        }
        String ruta = archivo.getUrlArchivo();
        if (ruta == null || ruta.isBlank()) {
            return "archivo";
        }
        int separador = ruta.lastIndexOf('/');
        return separador >= 0 ? ruta.substring(separador + 1) : ruta;
    }

    private void eliminarArchivosSilencioso(List<String> rutasGuardadas, RuntimeException ex) {
        for (String ruta : rutasGuardadas) {
            try {
                archivoStorage.eliminar(ruta);
            } catch (RuntimeException limpieza) {
                ex.addSuppressed(limpieza);
            }
        }
    }

    private EstadoExamen validarEstado(EstadoExamen estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        return estado;
    }

    private String validarNombre(String nombreExamen) {
        String normalizado = StringNormalizer.normalizarTexto(nombreExamen);
        if (normalizado.isBlank()) {
            throw new IllegalArgumentException("El nombre del examen es obligatorio");
        }
        return normalizado;
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

    private examenDTOs.Response toResponse(examenEntity entity) {
        List<examenDTOs.ArchivoResponse> archivos = entity.getArchivos().stream()
                .map(archivo -> new examenDTOs.ArchivoResponse(
                        archivo.getId(),
                        nombreOriginal(archivo),
                        archivo.getUrlArchivo(),
                        archivo.getTamano()))
                .toList();

        return new examenDTOs.Response(
                entity.getId(),
                entity.getNombreExamen(),
                entity.getObservacion(),
                archivos,
                entity.getEpisodio().getId(),
                entity.getUsuario().getId(),
                nombreUsuario(entity.getUsuario()),
                entity.getFecha(),
                entity.getEstado());
    }
}
