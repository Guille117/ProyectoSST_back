package com.example.demo.modules.pacientes.paciente;

import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoEpisodioServiceTest {

    @Mock private DocumentoEpisodioRepository repository;
    @Mock private EpisodioRepository episodioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DocumentoEpisodioStorage storage;

    @InjectMocks private DocumentoEpisodioService service;

    private final EpisodioEntity episodio = EpisodioEntity.builder().id(8L).build();

    private UsuarioEntity usuario() {
        PersonaEntity persona = PersonaEntity.builder()
                .primerNombre("Juan").primerApellido("Pérez").segundoApellido("López").build();
        return UsuarioEntity.builder().id(3L).username("jperez").persona(persona).estado(true).build();
    }

    private DocumentoEpisodioEntity documento(Long id, TipoDocumentoEpisodio tipo, boolean estado) {
        return DocumentoEpisodioEntity.builder()
                .id(id).episodio(episodio).tipo(tipo)
                .nombreOriginal("archivo.pdf").urlArchivo("uploads/documentos/archivo-" + id + ".pdf")
                .fechaSubida(LocalDateTime.of(2026, 10, 17, 10, 30))
                .usuario(usuario()).estado(estado).build();
    }

    private MockMultipartFile pdf(String nombre) {
        return new MockMultipartFile("archivos", nombre, "application/pdf", "%PDF-1.7\ncontenido".getBytes());
    }

    @Test
    void listar_debeFiltrarPorTipoYEstado() {
        List<DocumentoEpisodioEntity> docs = List.of(documento(1L, TipoDocumentoEpisodio.REFERENCIA, true));
        when(repository.findByEpisodio_IdAndTipoAndEstadoOrderByFechaSubidaDesc(
                8L, TipoDocumentoEpisodio.REFERENCIA, true)).thenReturn(docs);

        List<DocumentoEpisodioDTOs.Response> resultado = service.listar(
                8L, TipoDocumentoEpisodio.REFERENCIA, true);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).tipo()).isEqualTo(TipoDocumentoEpisodio.REFERENCIA);
        assertThat(resultado.get(0).nombreOriginal()).isEqualTo("archivo.pdf");
        assertThat(resultado.get(0).usuarioNombre()).isEqualTo("Juan Pérez López");
        assertThat(resultado.get(0).estado()).isTrue();
    }

    @Test
    void subir_debeGuardarVariosArchivosDelMismoTipo() {
        when(episodioRepository.findById(8L)).thenReturn(Optional.of(episodio));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario()));
        when(storage.guardar(any(), eq(8L), eq(TipoDocumentoEpisodio.REFERENCIA)))
                .thenReturn("uploads/documentos/uno.pdf", "uploads/documentos/dos.pdf");
        when(repository.save(any())).thenAnswer(invocation -> {
            DocumentoEpisodioEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(10L);
            }
            return entity;
        });

        List<DocumentoEpisodioDTOs.Response> resultado = service.subir(
                8L, TipoDocumentoEpisodio.REFERENCIA, 3L, List.of(pdf("uno.pdf"), pdf("dos.pdf")));

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(DocumentoEpisodioDTOs.Response::urlArchivo)
                .containsExactly("uploads/documentos/uno.pdf", "uploads/documentos/dos.pdf");
        verify(repository, times(2)).save(any());
    }

    @Test
    void subir_debeRechazarListaVacia() {
        assertThatThrownBy(() -> service.subir(8L, TipoDocumentoEpisodio.REFERENCIA, 3L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un archivo");

        verify(repository, never()).save(any());
    }

    @Test
    void guardarTodos_debeLimpiarArchivosSiFallaUnGuardado() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario()));
        when(storage.guardar(any(), eq(8L), eq(TipoDocumentoEpisodio.REFERENCIA)))
                .thenReturn("uploads/documentos/uno.pdf");
        when(storage.guardar(any(), eq(8L), eq(TipoDocumentoEpisodio.RESPONSABILIDAD)))
                .thenThrow(new IllegalArgumentException("fallo"));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<TipoDocumentoEpisodio, List<org.springframework.web.multipart.MultipartFile>> archivos =
                new LinkedHashMap<>();
        archivos.put(TipoDocumentoEpisodio.REFERENCIA, List.of(pdf("uno.pdf")));
        archivos.put(TipoDocumentoEpisodio.RESPONSABILIDAD, List.of(pdf("dos.pdf")));

        assertThatThrownBy(() -> service.guardarTodos(episodio, 3L, archivos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fallo");

        verify(storage).eliminar("uploads/documentos/uno.pdf");
    }

    @Test
    void eliminar_debeHacerBajaLogicaSinBorrarElArchivo() {
        DocumentoEpisodioEntity doc = documento(10L, TipoDocumentoEpisodio.REFERENCIA, true);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));

        service.eliminar(10L);

        assertThat(doc.isEstado()).isFalse();
        verify(repository).save(doc);
        verify(storage, never()).eliminar(any());
    }

    @Test
    void cambiarEstado_debeReactivarElDocumento() {
        DocumentoEpisodioEntity doc = documento(10L, TipoDocumentoEpisodio.REFERENCIA, false);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));
        when(repository.save(doc)).thenReturn(doc);

        DocumentoEpisodioDTOs.Response resultado = service.cambiarEstado(10L);

        assertThat(doc.isEstado()).isTrue();
        assertThat(resultado.estado()).isTrue();
    }

    @Test
    void cargarArchivo_debeRechazarDocumentoDadoDeBaja() {
        DocumentoEpisodioEntity doc = documento(10L, TipoDocumentoEpisodio.REFERENCIA, false);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> service.cargarArchivo(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dado de baja");

        verify(storage, never()).cargar(any());
    }

    @Test
    void cargarArchivo_debeRetornarElRecursoActivo() {
        DocumentoEpisodioEntity doc = documento(10L, TipoDocumentoEpisodio.REFERENCIA, true);
        DocumentoEpisodioStorage.Archivo archivo = new DocumentoEpisodioStorage.Archivo(
                new ByteArrayResource("contenido".getBytes()), MediaType.APPLICATION_PDF);
        when(repository.findById(10L)).thenReturn(Optional.of(doc));
        when(storage.cargar(doc.getUrlArchivo())).thenReturn(archivo);

        assertThat(service.cargarArchivo(10L)).isSameAs(archivo);
    }
}
