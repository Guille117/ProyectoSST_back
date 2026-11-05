package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.clinica.alta.altaEntity;
import com.example.demo.modules.clinica.alta.altaRepository;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoEntity;
import com.example.demo.modules.clinica.procedimientoMedico.procedimientoMedicoRepository;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentoEpisodioService {

    private final DocumentoEpisodioRepository repository;
    private final EpisodioRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final DocumentoEpisodioStorage storage;
    private final procedimientoMedicoRepository procedimientoMedicoRepository;
    private final altaRepository altaRepository;

    @Transactional(readOnly = true)
    public List<DocumentoEpisodioDTOs.Response> listar(
            Long episodioId, TipoDocumentoEpisodio tipo, Boolean activos) {
        if (episodioId == null) {
            throw new IllegalArgumentException("El episodio es obligatorio");
        }
        boolean estado = activos == null || activos;
        List<DocumentoEpisodioEntity> documentos = tipo == null
                ? repository.findByEpisodio_IdAndEstadoOrderByFechaSubidaDesc(episodioId, estado)
                : repository.findByEpisodio_IdAndTipoAndEstadoOrderByFechaSubidaDesc(episodioId, tipo, estado);
        return documentos.stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<DocumentoEpisodioDTOs.Response> subir(
            Long episodioId, TipoDocumentoEpisodio tipo, Long usuarioId, List<MultipartFile> archivos) {
        return subir(episodioId, tipo, usuarioId, null, null, archivos);
    }

    @Transactional
    public List<DocumentoEpisodioDTOs.Response> subir(
            Long episodioId, TipoDocumentoEpisodio tipo, Long usuarioId,
            Long procedimientoMedicoId, Long altaId, List<MultipartFile> archivos) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        if (archivos == null || archivos.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un archivo");
        }
        EpisodioEntity episodio = buscarEpisodio(episodioId);
        procedimientoMedicoEntity procedimiento = resolverProcedimiento(
                tipo, episodio, procedimientoMedicoId);
        altaEntity alta = resolverAlta(tipo, episodio, altaId);
        UsuarioEntity usuario = buscarUsuario(usuarioId);
        return guardar(episodio, usuario, tipo, procedimiento, alta, archivos);
    }

    /** Guarda varios tipos de documentos durante el alta del paciente. */
    @Transactional
    public List<DocumentoEpisodioEntity> guardarTodos(
            EpisodioEntity episodio, Long usuarioId, Map<TipoDocumentoEpisodio, List<MultipartFile>> archivos) {
        if (archivos == null || archivos.isEmpty()) {
            return List.of();
        }
        UsuarioEntity usuario = buscarUsuario(usuarioId);
        List<DocumentoEpisodioEntity> guardados = new ArrayList<>();
        List<String> rutasGuardadas = new ArrayList<>();
        try {
            for (Map.Entry<TipoDocumentoEpisodio, List<MultipartFile>> entrada : archivos.entrySet()) {
                guardar(episodio, usuario, entrada.getKey(), null, null,
                        entrada.getValue(), guardados, rutasGuardadas);
            }
            return guardados;
        } catch (RuntimeException ex) {
            eliminarArchivosSilencioso(rutasGuardadas, ex);
            throw ex;
        }
    }

    @Transactional
    public DocumentoEpisodioDTOs.Response cambiarEstado(Long id) {
        DocumentoEpisodioEntity documento = buscarDocumento(id);
        documento.setEstado(!documento.isEstado());
        return toResponse(repository.save(documento));
    }

    /** Baja lógica: conserva el registro y el archivo físico. */
    @Transactional
    public void eliminar(Long id) {
        DocumentoEpisodioEntity documento = buscarDocumento(id);
        if (documento.isEstado()) {
            documento.setEstado(false);
            repository.save(documento);
        }
    }

    @Transactional(readOnly = true)
    public DocumentoEpisodioStorage.Archivo cargarArchivo(Long id) {
        DocumentoEpisodioEntity documento = buscarDocumento(id);
        if (!documento.isEstado()) {
            throw new IllegalArgumentException("El documento está dado de baja");
        }
        return storage.cargar(documento.getUrlArchivo());
    }

    private List<DocumentoEpisodioDTOs.Response> guardar(
            EpisodioEntity episodio, UsuarioEntity usuario, TipoDocumentoEpisodio tipo,
            procedimientoMedicoEntity procedimiento, altaEntity alta, List<MultipartFile> archivos) {
        List<DocumentoEpisodioEntity> guardados = new ArrayList<>();
        List<String> rutasGuardadas = new ArrayList<>();
        try {
            guardar(episodio, usuario, tipo, procedimiento, alta,
                    archivos, guardados, rutasGuardadas);
            return guardados.stream().map(this::toResponse).toList();
        } catch (RuntimeException ex) {
            eliminarArchivosSilencioso(rutasGuardadas, ex);
            throw ex;
        }
    }

    private void guardar(
            EpisodioEntity episodio, UsuarioEntity usuario, TipoDocumentoEpisodio tipo,
            procedimientoMedicoEntity procedimiento, altaEntity alta,
            List<MultipartFile> archivos, List<DocumentoEpisodioEntity> guardados,
            List<String> rutasGuardadas) {
        if (archivos == null || archivos.isEmpty()) {
            return;
        }
        for (MultipartFile archivo : archivos) {
            if (archivo == null) {
                continue;
            }
            String ruta = storage.guardar(archivo, episodio.getId(), tipo);
            rutasGuardadas.add(ruta);
            DocumentoEpisodioEntity documento = repository.save(DocumentoEpisodioEntity.builder()
                    .episodio(episodio)
                    .tipo(tipo)
                    .nombreOriginal(nombreOriginal(archivo))
                    .urlArchivo(ruta)
                    .fechaSubida(LocalDateTime.now())
                    .usuario(usuario)
                    .procedimientoMedico(procedimiento)
                    .alta(alta)
                    .estado(true)
                    .build());
            guardados.add(documento);
        }
    }

    private procedimientoMedicoEntity resolverProcedimiento(
            TipoDocumentoEpisodio tipo, EpisodioEntity episodio, Long procedimientoMedicoId) {
        if (tipo != TipoDocumentoEpisodio.AUTORIZACION_PROCEDIMIENTO) {
            return null;
        }
        if (procedimientoMedicoId == null) {
            throw new IllegalArgumentException("El procedimiento es obligatorio para la autorización");
        }
        procedimientoMedicoEntity procedimiento = procedimientoMedicoRepository
                .findById(procedimientoMedicoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Procedimiento no encontrado con el ID: " + procedimientoMedicoId));
        if (!procedimiento.isRequiereAutorizacion()) {
            throw new IllegalArgumentException("El procedimiento no requiere autorización");
        }
        if (procedimiento.getEpisodio() == null
                || !procedimiento.getEpisodio().getId().equals(episodio.getId())) {
            throw new IllegalArgumentException("El procedimiento no pertenece al episodio");
        }
        return procedimiento;
    }

    private altaEntity resolverAlta(
            TipoDocumentoEpisodio tipo, EpisodioEntity episodio, Long altaId) {
        if (tipo != TipoDocumentoEpisodio.ALTA_MEDICA && tipo != TipoDocumentoEpisodio.RECETA) {
            return null;
        }
        if (altaId == null) {
            throw new IllegalArgumentException("El alta es obligatoria para el documento");
        }
        altaEntity alta = altaRepository.findById(altaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Alta no encontrada con el ID: " + altaId));
        if (alta.getEpisodio() == null
                || !alta.getEpisodio().getId().equals(episodio.getId())) {
            throw new IllegalArgumentException("El alta no pertenece al episodio");
        }
        return alta;
    }

    private void eliminarArchivosSilencioso(List<String> rutasGuardadas, RuntimeException ex) {
        for (String ruta : rutasGuardadas) {
            try {
                storage.eliminar(ruta);
            } catch (RuntimeException limpieza) {
                ex.addSuppressed(limpieza);
            }
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

    private DocumentoEpisodioEntity buscarDocumento(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado con el ID: " + id));
    }

    private DocumentoEpisodioDTOs.Response toResponse(DocumentoEpisodioEntity documento) {
        UsuarioEntity usuario = documento.getUsuario();
        String nombreUsuario = usuario.getPersona() != null
                ? usuario.getPersona().getNombreCompleto()
                : usuario.getUsername();
        return new DocumentoEpisodioDTOs.Response(
                documento.getId(),
                documento.getTipo(),
                documento.getNombreOriginal(),
                documento.getUrlArchivo(),
                documento.getFechaSubida(),
                usuario.getId(),
                nombreUsuario,
                documento.isEstado(),
                documento.getProcedimientoMedico() != null
                        ? documento.getProcedimientoMedico().getId() : null,
                documento.getAlta() != null ? documento.getAlta().getId() : null);
    }
}
