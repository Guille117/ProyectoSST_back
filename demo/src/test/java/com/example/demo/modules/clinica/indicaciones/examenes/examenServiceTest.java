package com.example.demo.modules.clinica.indicaciones.examenes;

import com.example.demo.modules.pacientes.paciente.EpisodioEntity;
import com.example.demo.modules.pacientes.paciente.EpisodioRepository;
import com.example.demo.modules.usuarios.usuarios.PersonaEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioEntity;
import com.example.demo.modules.usuarios.usuarios.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class examenServiceTest {

    @Mock
    private examenRepository repository;

    @Mock
    private examenArchivoRepository archivoRepository;

    @Mock
    private ExamenArchivoStorage archivoStorage;

    @Mock
    private EpisodioRepository episodioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private examenService service;

    private final MultipartFile archivo = new MockMultipartFile(
            "archivos", "hemograma.pdf", "application/pdf", "%PDF-1.7\ncontenido".getBytes());

    @Test
    void crear_sinArchivos_debeGuardarElExamen() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(examenEntity.class))).thenAnswer(invocation -> {
            examenEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        examenDTOs.Response response = service.crear(
                new examenDTOs.Request("  Hemograma ", "  En ayunas ", 1L, 2L), List.of());

        assertEquals(10L, response.id());
        assertEquals("Hemograma", response.nombreExamen());
        assertEquals("En ayunas", response.observacion());
        assertEquals("Carlos Mendez", response.usuarioNombre());
        assertTrue(response.archivos().isEmpty());
        assertEquals(EstadoExamen.ACTIVO, response.estado());
        assertNotNull(response.fecha());
        verifyNoInteractions(archivoStorage);
    }

    @Test
    void crear_conArchivos_debeGuardarLasUrls() {
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(repository.save(any(examenEntity.class))).thenAnswer(invocation -> {
            examenEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(10L);
            }
            return entity;
        });
        when(archivoStorage.guardar(any(), eq(10L))).thenReturn("uploads/examenes/examen-10-a.pdf");
        when(archivoRepository.save(any(examenArchivoEntity.class))).thenAnswer(invocation -> {
            examenArchivoEntity entity = invocation.getArgument(0);
            entity.setId(50L);
            return entity;
        });

        examenDTOs.Response response = service.crear(
                new examenDTOs.Request("Hemograma", null, 1L, 2L), List.of(archivo));

        assertEquals(1, response.archivos().size());
        assertEquals(50L, response.archivos().get(0).id());
        assertEquals("uploads/examenes/examen-10-a.pdf", response.archivos().get(0).urlArchivo());
        verify(archivoStorage).guardar(archivo, 10L);
    }

    @Test
    void crear_debeRechazarNombreVacio() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(new examenDTOs.Request("   ", null, 1L, 2L), List.of()));

        assertEquals("El nombre del examen es obligatorio", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void actualizar_debeAgregarNuevosArchivos() {
        examenEntity existente = examenEntity.builder()
                .id(10L)
                .nombreExamen("Hemograma")
                .archivos(new ArrayList<>(List.of(examenArchivoEntity.builder()
                        .id(40L).urlArchivo("uploads/examenes/examen-10-previo.pdf").build())))
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 3, 5, 16, 20))
                .estado(EstadoExamen.ACTIVO)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(episodioRepository.findById(1L)).thenReturn(Optional.of(episodio(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, "Carlos Mendez")));
        when(archivoStorage.guardar(any(), eq(10L))).thenReturn("uploads/examenes/examen-10-nuevo.pdf");
        when(archivoRepository.save(any(examenArchivoEntity.class))).thenAnswer(invocation -> {
            examenArchivoEntity entity = invocation.getArgument(0);
            entity.setId(51L);
            return entity;
        });
        when(repository.save(any(examenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        examenDTOs.Response response = service.actualizar(10L,
                new examenDTOs.Request("Hemograma completo", "Revisar", 1L, 2L), List.of(archivo));

        assertEquals("Hemograma completo", response.nombreExamen());
        assertEquals(2, response.archivos().size());
        assertEquals("uploads/examenes/examen-10-nuevo.pdf", response.archivos().get(1).urlArchivo());
    }

    @Test
    void eliminarArchivo_debeQuitarElRegistroYBorrarElFisico() {
        examenEntity existente = examenEntity.builder()
                .id(10L)
                .nombreExamen("Hemograma")
                .archivos(new ArrayList<>(List.of(examenArchivoEntity.builder()
                        .id(50L).urlArchivo("uploads/examenes/examen-10-a.pdf").build())))
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 3, 5, 16, 20))
                .estado(EstadoExamen.ACTIVO)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(existente));
        when(repository.save(any(examenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminarArchivo(10L, 50L);

        assertTrue(existente.getArchivos().isEmpty());
        verify(archivoStorage).eliminar("uploads/examenes/examen-10-a.pdf");
    }

    @Test
    void eliminarArchivo_debeLanzarCuandoNoPertenece() {
        examenEntity existente = examenEntity.builder()
                .id(10L)
                .nombreExamen("Hemograma")
                .archivos(new ArrayList<>())
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 3, 5, 16, 20))
                .estado(EstadoExamen.ACTIVO)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(existente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.eliminarArchivo(10L, 99L));

        assertEquals("Archivo del examen no encontrado con el ID: 99", exception.getMessage());
        verifyNoInteractions(archivoStorage);
    }

    @Test
    void obtenerPorId_debeLanzarCuandoNoExiste() {
        when(repository.findById(77L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(77L));

        assertEquals("Examen no encontrado con el ID: 77", exception.getMessage());
    }

    @Test
    void cambiarEstado_debeAsignarElNuevoEstado() {
        examenEntity existente = entidad(7L, EstadoExamen.ACTIVO);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(examenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        examenDTOs.Response response = service.cambiarEstado(7L, EstadoExamen.ELIMINADO);

        assertEquals(EstadoExamen.ELIMINADO, response.estado());
        verify(repository).save(existente);
    }

    @Test
    void eliminar_debeMarcarElRegistroComoEliminado() {
        examenEntity existente = entidad(7L, EstadoExamen.ACTIVO);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(examenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.eliminar(7L);

        assertEquals(EstadoExamen.ELIMINADO, existente.getEstado());
        verify(repository).save(existente);
    }

    private examenEntity entidad(Long id, EstadoExamen estado) {
        return examenEntity.builder()
                .id(id)
                .nombreExamen("Hemograma")
                .episodio(episodio(1L))
                .usuario(usuario(2L, "Carlos Mendez"))
                .fecha(LocalDateTime.of(2026, 3, 5, 16, 20))
                .estado(estado)
                .build();
    }

    private EpisodioEntity episodio(Long id) {
        return EpisodioEntity.builder().id(id).descripcion("Episodio de prueba").build();
    }

    private UsuarioEntity usuario(Long id, String nombreCompleto) {
        PersonaEntity persona = new PersonaEntity();
        persona.asignarNombres(nombreCompleto, null);
        return UsuarioEntity.builder().id(id).username("usuario" + id).persona(persona).build();
    }
}
