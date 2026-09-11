# AGENTS.md

## Estructura del proyecto
- Root de Git es `backSST/` pero el proyecto Gradle/Spring Boot está en `demo/` — todos los comandos `gradlew` se ejecutan con `cwd: demo`.
- Stack: Java 21 (toolchain), Spring Boot 4.1.0 (webmvc + data-jpa + validation + security), Gradle 9.5.1, Lombok + MapStruct 1.5.5, MySQL, JWT (jjwt 0.12.5), springdoc 2.8.5. Ver `demo/build.gradle:1-58`.
- Entrypoint: `demo/src/main/java/com/example/demo/DemoApplication.java:1`
- Módulos: `demo/src/main/java/com/example/demo/modules/{farmacia/usuarios}/[submodulo]` + transversales `security/`, `config/`, `utils/`. Ver `demo/IA_RULES.md:1` para el patrón autoritativo de 8 archivos.

## Comandos — ejecutar desde `demo/`
```bat
.\gradlew.bat bootRun          # requiere MySQL (ver application.properties)
.\gradlew.bat test              # no requiere BD — solo Mockito/WebMvcTest
.\gradlew.bat test --tests "com.example.demo.modules.farmacia.proveedores.ProveedorServiceTest"
.\gradlew.bat test --tests "com.example.demo.modules.farmacia.proveedores.ProveedorServiceTest.crear_debeGuardarCuandoDatosSonValidos"
.\gradlew.bat build             # compila + ejecuta tests
```
- Wrapper es `demo/gradlew.bat` (Windows) / `demo/gradlew` (Unix). Las configs de VS Code ya fijan `cwd` en `demo` (`.vscode/launch.json:10`, `.vscode/tasks.json:12`).
- No hay lint/formatter/typecheck configurado — `build` y `test` son los únicos pasos de verificación.

## Requisitos de ejecución
- `demo/src/main/resources/application.properties:3` requiere MySQL en `localhost:3306/sst_db` (`sst_user`/`daguerShuriken`), `ddl-auto=update`. `bootRun` falla sin BD; los tests no la necesitan.
- Props JWT: `jwt.secret` / `jwt.expiration` (`application.properties:12`). OpenAPI en `/v3/api-docs`, Swagger UI deshabilitado, Scalar UI habilitado.

## Arquitectura y convenciones

### Patrón de 8 archivos por submódulo (`demo/IA_RULES.md:12`)
Cada tabla/funcionalidad `[Entidad]` bajo `com.example.demo.modules.[modulo].[submodulo]` debe tener exactamente 6 archivos prod + 2 de test en el mismo paquete:

1. `[Entidad]Entity.java` — JPA + Lombok `@Entity @Table @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`, `Long id` `IDENTITY`, `@Builder.Default boolean estado = true`
2. `[Entidad]DTOs.java` — clase contenedora con `record Request(...)` (validación Jakarta) y `record Response(...)`
3. `[Entidad]Mapper.java` — `@Mapper(componentModel="spring")`, ignora `id`/`codigo`, `estado` con expresión `request.estado()!=null ? request.estado() : true`
4. `[Entidad]Repository.java` — `@Repository extends JpaRepository<Entity,Long>`, queries derivadas (`findByNombreIgnoreCase`, `ContainingIgnoreCase`, `findByEstado`)
5. `[Entidad]Service.java` — `@Service @RequiredArgsConstructor`, `@Transactional` / `readOnly=true`, `StringNormalizer` para entradas, `validarNoDuplicado` antes de guardar, generación de código `PREF-%02d` vía `repository.count()+1`
6. `[Entidad]Controller.java` — `@RestController @RequestMapping("/api/v1/[submodulo]")`, endpoints `POST /` (201), `GET /`, `GET /buscar`, `GET /{id}`, `PUT /{id}`, `PATCH /{id}` (toggle estado)
7. `[Entidad]ServiceTest.java` — `@ExtendWith(MockitoExtension.class)`, `@Mock Repository/Mapper`, `@InjectMocks Service`, Given-When-Then (crear/obtenerPorId/actualizar/cambiarEstado + casos de duplicado)
8. `[Entidad]ControllerTest.java` — `@WebMvcTest(Controller.class)`, `@Autowired MockMvc/ObjectMapper`, `@MockitoBean Service`, asserts `isCreated()/isOk()/isBadRequest()`

Implementación de referencia: `demo/src/main/java/com/example/demo/modules/farmacia/proveedores/` (proveedores es el ejemplo canónico).

### Otras convenciones
- `StringNormalizer` (`demo/src/main/java/com/example/demo/utils/StringNormalizer.java:8`): `normalizarTexto` (null→"" y trim), `normalizarNullable` (blank→null). El Service normaliza cada campo antes de validar/persistir; `nit` además hace `toUpperCase(ROOT)`.
- Validación de duplicados distingue activos vs inactivos con mensajes distintos: `"Ya existe un proveedor activo con..."` vs `"...pero está inactivo. Actívalo primero..."` (`ProveedorService.java:124`).
- Validación NIT/teléfono en service: `CF` o `^[0-9]{7}-[0-9Kk]$` para NIT, `^[0-9]{8}$` para teléfono; la capa DTO tiene `@Pattern` más permisivo que permite string vacío.
- `ApiExceptionHandler` (`demo/src/main/java/com/example/demo/config/ApiExceptionHandler.java:17`) mapea `IllegalArgumentException`/`RuntimeException`→400, `HorarioAccessException`→403, `BadCredentialsException`→401, `MethodArgumentNotValid`→400 (primer field error).
- Auth: `SecurityConfig` (`demo/src/main/java/com/example/demo/security/SecurityConfig.java:49`) actualmente `permitAll` para `/api/v1/**` y docs (modo dev) pero el filtro JWT + checks de horario en `AuthService` (`AuthService.java:80` — bypass rol ADMINISTRADOR / puestos Administrador-Director, bypass rotativo, check detalle semanal) ya están cableados para futuro bloqueo.

## Testing
- Todos los tests actuales son unitarios/slice — sin Testcontainers, sin H2, sin `@SpringBootTest` con BD. `DemoApplicationTests` es el único test de integración.
- Patrón: `ServiceTest` solo Mockito; `ControllerTest` usa `@WebMvcTest` + `@MockitoBean`.
- Ejecución focalizada: `.\gradlew.bat test --tests "*.ProveedorServiceTest"` — para iterar rápido usar el nombre calificado del método.

## Errores comunes
- Ejecutar gradle desde el root (`backSST/`) falla — `settings.gradle` está dentro de `demo/`. Siempre `cd demo` o fijar `cwd`.
- `.vscode/` en el root está trackeado; `demo/.vscode/` y `demo/build/` / `demo/.gradle/` / `demo/bin/` están en `.gitignore` (`demo/.gitignore:37`). No commitear artefactos de build.
- `ProveedorService.obtenerTodos` actualmente ignora el caso `activos==null` e imprime a stdout (`ProveedorService.java:84-95`); seguir el estilo TODO existente si se corrige.
- Nuevos submódulos deben quedar en `com.example.demo.modules.*` — el component scan de `DemoApplication` es `com.example.demo` (`DemoApplication.java:10`).
- Rama activa es `develop`; último commit `ef1e7ab primera versión de submódulo de proveedores...`.
