# Directrices de Desarrollo Backend - Spring Boot 4 / Java 21

## Arquitectura y Estructura de Paquetes
- **Patrón:** Modular por dominio de negocio (Feature/Subfeature).
- **Ruta de paquetes de producción:** `com.example.demo.modules.[modulo].[submodulo]` en `src/main/java/`
- **Ruta de paquetes de pruebas:** `com.example.demo.modules.[modulo].[submodulo]` en `src/test/java/`
- Todos los archivos del submódulo deben residir en la misma carpeta del submódulo.

---

## Convención de Archivos por Submódulo
Para cada tabla o funcionalidad `[Entidad]`, se deben generar exactamente estos 8 archivos (6 de producción + 2 de pruebas):

### Código de Producción (`src/main/java`)

1. **`[Entidad]Entity.java`**:
   - Mapeo JPA con Jakarta (`jakarta.persistence.*`).
   - Anotaciones Lombok: `@Entity`, `@Table(name = "...")`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
   - Llave primaria `Long id` con `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
   - Campo de auditoría: `@Builder.Default private boolean estado = true;`.

2. **`[Entidad]DTOs.java`**:
   - Clase contenedora con dos `record` estáticos internos:
     - `public record Request(...) {}`: con validaciones Jakarta (`@NotBlank`, `@Pattern`, `@Email`, etc.).
     - `public record Response(...) {}`: con los campos de lectura públicos.

3. **`[Entidad]Mapper.java`**:
   - Interfaz anotada con `@Mapper(componentModel = "spring")` (MapStruct).
   - Métodos:
     - `@Mapping(target = "id", ignore = true)`
     - `@Mapping(target = "codigo", ignore = true)`
     - `@Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")`
     - `[Entidad]Entity toEntity([Entidad]DTOs.Request request);`
     - `[Entidad]DTOs.Response toDTO([Entidad]Entity entity);`

4. **`[Entidad]Repository.java`**:
   - Interfaz anotada con `@Repository` que extiende de `JpaRepository<[Entidad]Entity, Long>`.
   - Consultas derivadas por nombre, unicidad, estado y búsquedas con `ContainingIgnoreCase`.

5. **`[Entidad]Service.java`**:
   - Clase anotada con `@Service` y `@RequiredArgsConstructor`.
   - Métodos transaccionales con `@Transactional` y `@Transactional(readOnly = true)`.
   - Normalización de entradas con `StringNormalizer`.
   - Validaciones estrictas de duplicados (`validarNoDuplicado`) antes de persistir.
   - Generación de código prefijado (ej. `PREF-%02d`).
   - Métodos estándar: `crear`, `actualizar`, `obtenerPorId`, `obtenerTodos`, `buscarPorNombre`, `cambiarEstado`.

6. **`[Entidad]Controller.java`**:
   - `@RestController` y `@RequestMapping("/api/v1/[submodulo]")` con `@RequiredArgsConstructor`.
   - Endpoints: `POST /` (201 Created), `GET /`, `GET /buscar`, `GET /{id}`, `PUT /{id}`, `PATCH /{id}` (alternar estado).

---

### Pruebas Unitarias (`src/test/java`)

7. **`[Entidad]ServiceTest.java`**:
   - Clase anotada con `@ExtendWith(MockitoExtension.class)`.
   - Mocks: `@Mock private [Entidad]Repository repository;` y `@Mock private [Entidad]Mapper mapper;`.
   - Inyección: `@InjectMocks private [Entidad]Service service;`.
   - Pruebas unitarias completas con `@Test` y metodología **Given-When-Then**:
     - `crear_Exitoso`: Guarda entidad normalizada y retorna DTO response.
     - `crear_LanzaExcepcion_CuandoDatoDuplicado`: Simula duplicado y verifica que lance excepción.
     - `obtenerPorId_Exitoso`: Retorna DTO cuando existe el ID.
     - `obtenerPorId_LanzaExcepcion_CuandoNoExiste`: Verifica error si el Optional está vacío.
     - `actualizar_Exitoso`: Modifica datos y retorna DTO actualizado.
     - `cambiarEstado_Exitoso`: Invierte el valor booleano del estado.

8. **`[Entidad]ControllerTest.java`**:
   - Anotada con `@WebMvcTest([Entidad]Controller.class)`.
   - Inyección de `@Autowired private MockMvc mockMvc;` y `@Autowired private ObjectMapper objectMapper;`.
   - `@MockitoBean private [Entidad]Service service;`.
   - Pruebas de endpoints verificando status HTTP y estructura JSON:
     - `POST /api/v1/[submodulo]` $\rightarrow$ `status().isCreated()`.
     - `POST /api/v1/[submodulo]` con campos inválidos $\rightarrow$ `status().isBadRequest()`.
     - `GET /api/v1/[submodulo]/{id}` $\rightarrow$ `status().isOk()`.
     - `PUT /api/v1/[submodulo]/{id}` $\rightarrow$ `status().isOk()`.
     - `PATCH /api/v1/[submodulo]/{id}` $\rightarrow$ `status().isOk()`.