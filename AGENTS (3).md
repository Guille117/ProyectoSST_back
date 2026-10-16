# AGENTS.md — Backend del Sanatorio Santa Teresita

> **Ámbito:** repositorio `backSST/`, con el proyecto Java/Spring Boot y Gradle en `demo/`. Estas instrucciones se aplican a OpenCode, Cline y otros agentes que trabajen en el backend. Angular es un proyecto independiente.
>
> **Objetivo:** implementar únicamente lo solicitado con código correcto, seguro, mantenible y reutilizable. Trabajar rápidamente en cambios pequeños y aplicar comprobaciones más profundas cuando el riesgo lo requiera.
>
> **Documento único:** este archivo contiene las convenciones técnicas y las reglas de trabajo vigentes. No depende de otros archivos Markdown de instrucciones. La estructura de ocho archivos es una **referencia flexible**, no una obligación universal.

## 1. Principios y prioridades

Priorizar, en este orden:

1. Funcionamiento correcto, integridad de los datos y seguridad.
2. Respeto al alcance, contratos públicos y comportamiento existente.
3. Reutilización de código y convenciones verificadas en el proyecto.
4. Claridad, organización, ausencia de duplicaciones y limpieza de código obsoleto.
5. Eficiencia de ejecución y verificaciones proporcionales.

Reglas generales:

- Antes de editar, localizar y leer los archivos implicados y sus dependencias directas. No explorar el repositorio completo por rutina.
- No inventar campos, endpoints, permisos, respuestas, tablas, validaciones de negocio ni funcionalidades no solicitadas.
- No renombrar ni mover clases, paquetes, rutas o propiedades públicas por una preferencia de estilo.
- No introducir librerías, frameworks, abstracciones o carpetas adicionales sin necesidad real. Pedir autorización para cambios significativos de dependencias o arquitectura.
- No modificar módulos no relacionados. Si surge un problema externo al alcance, mencionarlo sin arreglarlo automáticamente.
- No cambiar este `AGENTS.md`, scripts de Gradle, configuración de seguridad o de base de datos por iniciativa propia. Hacerlo únicamente si la tarea lo requiere y está justificado.
- Cuando una regla general no encaje con una funcionalidad específica, prevalece la lógica del dominio y la arquitectura comprobada; no forzar plantillas mecánicamente.
- Antes de una operación destructiva o de efectos difíciles de revertir sobre datos reales, solicitar autorización explícita.

## 2. Niveles de intervención y rapidez de Cline/OpenCode

Determinar el nivel automáticamente a partir del alcance **y del riesgo**, no solo del número de líneas.

### Nivel 1 — Ajuste pequeño

Ejemplos: cambiar un mensaje, una anotación no crítica, un nombre local, una línea o un método sencillo sin alterar un contrato ni datos importantes.

- Leer y editar solo los archivos indispensables.
- No hacer auditorías globales, búsquedas por todos los módulos, nuevas pruebas por cambios triviales, ni iniciar la aplicación.
- No ejecutar `build`, todas las pruebas ni compilaciones completas por defecto.
- Comprobar el diff, las referencias inmediatas y la sintaxis visible; eliminar solo restos generados por el propio cambio.
- Finalizar con una o dos frases. No producir un informe largo.
- **Excepción:** si el cambio afecta autorización, datos clínicos, transacciones, persistencia o una regla crítica, pasar al nivel 2 o 3 aunque el cambio sea corto.

### Nivel 2 — Cambio funcional

Ejemplos: modificar validaciones, búsquedas, lógica de Service, DTO, Mapper, consultas o un endpoint existente.

- Inspeccionar la funcionalidad y dependencias directamente relacionadas.
- Conservar los contratos utilizados por Angular, salvo instrucción explícita.
- Ajustar las pruebas relevantes si cambió el comportamiento.
- Ejecutar la compilación o pruebas focalizadas que aporten información útil, evitando repeticiones sin cambios.
- Revisar duplicaciones, código residual y errores dentro del alcance.

### Nivel 3 — Implementación o cambio importante

Ejemplos: submódulo completo, operaciones de varias capas, persistencia nueva, movimientos de inventario, seguridad, transacciones o nuevos contratos API.

- Elaborar un plan técnico interno breve y ejecutar por responsabilidades, sin pedir al usuario que solicite cada archivo por separado.
- Implementar coherentemente las capas necesarias y pruebas de comportamiento relevantes.
- Comprobar contratos, integridad de datos, autorizaciones, errores y concurrencia según el caso.
- Auditar el código afectado; no toda la aplicación si no es necesario.
- Ejecutar verificaciones amplias solo cuando el alcance y el riesgo las justifiquen.

**Regla transversal de eficiencia:** no ejecutar comandos repetidos cuando no han cambiado las causas del fallo. No iniciar procesos permanentes como verificación puntual. No convertir una corrección sencilla en una refactorización extensa.

## 3. Entorno técnico y comandos

Configuración conocida del proyecto (verificar los archivos reales si cambia):

- Java **21** mediante toolchain.
- Spring Boot **4.1.0**, Spring Web MVC, Spring Data JPA, Jakarta Validation y Spring Security.
- Gradle Wrapper (documentado como **9.5.1**).
- MySQL Connector/J, Lombok, MapStruct **1.5.5.Final** y `lombok-mapstruct-binding` **0.2.0**.
- JWT: `jjwt` **0.12.5**. Documentación OpenAPI: `springdoc-openapi-starter-webmvc-ui` **2.8.5**.
- Pruebas con JUnit Platform, Mockito, pruebas de servicios y pruebas web de Spring.

**Todas las órdenes de Gradle deben ejecutarse desde `backSST/demo/`**, donde están `build.gradle`, `settings.gradle` y el wrapper. En Windows/PowerShell:

```powershell
# Situarse primero en backSST/demo/
.\gradlew.bat compileJava
.\gradlew.bat test --tests "com.example.demo.modules.farmacia.proveedores.ProveedorServiceTest"
.\gradlew.bat test --tests "com.example.demo.modules.farmacia.proveedores.ProveedorServiceTest.crear_debeGuardarCuandoDatosSonValidos"
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat bootRun
```

- En Linux/macOS utilizar `./gradlew` dentro de `demo/`.
- `bootRun` exige una base de datos disponible y queda ejecutándose; no emplearlo como prueba de cambios pequeños.
- `build` compila y ejecuta pruebas: no repetirlo cuando una comprobación focalizada es suficiente.
- No asumir que existen lint, formatter o verificadores adicionales; comprobar antes de usarlos.
- No instalar ni actualizar dependencias para resolver tareas no relacionadas. Antes de modificar bibliotecas, revisar compatibilidad entre las versiones instaladas, especialmente Spring Boot, springdoc y MapStruct.
- Los comandos de prueba pueden depender de perfiles o infraestructura específicos; no afirmar que son independientes de la base de datos sin verificarlo para las pruebas afectadas.

## 4. Arquitectura y estructura de carpetas

```text
backSST/
├── AGENTS.md                       # Único archivo de reglas del agente
└── demo/                           # Raíz real de Gradle/Spring Boot
    ├── build.gradle
    ├── settings.gradle
    ├── gradlew
    ├── gradlew.bat
    └── src/
        ├── main/
        │   ├── java/com/example/demo/
        │   │   ├── DemoApplication.java
        │   │   ├── config/
        │   │   ├── security/
        │   │   ├── utils/
        │   │   └── modules/
        │   │       ├── catalogo/
        │   │       ├── clinica/
        │   │       ├── farmacia/
        │   │       ├── pacientes/
        │   │       └── usuarios/
        │   └── resources/
        │       ├── application.properties
        │       └── db/
        └── test/java/com/example/demo/
```

- Organización modular por dominio y subdominio: `com.example.demo.modules.[modulo].[submodulo]`.
- Conservar las convenciones de paquetes, nombres y separación de responsabilidades que ya utilice cada submódulo. No mover clases solo para uniformar.
- Ubicar las pruebas en el paquete equivalente bajo `src/test/java`.
- Reutilizar funciones de `config/`, `security/` y `utils/` cuando la tarea lo exija; no duplicar servicios globales.
- La carpeta `farmacia/proveedores` es una **referencia para CRUD**, no un molde obligatorio para procesos clínicos, seguridad ni movimientos de farmacia.
- Antes de crear una nueva clase, comprobar si ya existe una que resuelva la misma responsabilidad.

## 5. Patrón flexible de ocho archivos para CRUD

Para una entidad o funcionalidad CRUD convencional, tomar como base **seis clases de producción y dos clases de pruebas**:

| Archivo | Propósito |
|---|---|
| `[Entidad]Entity.java` | Modelo persistente JPA y sus relaciones. |
| `[Entidad]DTOs.java` | Contratos de solicitud y respuesta con validaciones. |
| `[Entidad]Mapper.java` | Conversiones entre Entity y DTO, preferiblemente MapStruct si ya se usa. |
| `[Entidad]Repository.java` | Persistencia y consultas Spring Data JPA. |
| `[Entidad]Service.java` | Reglas de negocio, normalización y transacciones. |
| `[Entidad]Controller.java` | Endpoints, validación de entrada y códigos HTTP. |
| `[Entidad]ServiceTest.java` | Pruebas del comportamiento del servicio. |
| `[Entidad]ControllerTest.java` | Pruebas del contrato HTTP y validaciones. |

**Es un patrón predeterminado, NO una exigencia de exactamente ocho archivos.**

- Crear solo las capas que realmente se necesiten. Reutilizar Entity, Repository, Service, DTO o Mapper existentes cuando correspondan.
- Para operaciones sin entidad propia, no generar tablas o repositorios ficticios.
- Para un proceso complejo, dividir responsabilidades únicamente cuando aporte cohesión y mantenibilidad; no reducir todo a un CRUD artificial.
- No crear archivos vacíos, implementaciones duplicadas ni clases que solo deleguen sin razón.
- Conservar los nombres existentes. Cuando un módulo ya use otra organización justificada, respetarla.
- Las pruebas deben cubrir comportamientos importantes; su ubicación y cantidad pueden adaptarse a la funcionalidad.

### Convenciones orientativas del patrón

- **Entidad:** anotaciones Jakarta JPA y Lombok según el proyecto (`@Entity`, `@Table`, `@Getter`, `@Setter`, `@NoArgsConstructor`, etc.). Si corresponde una clave generada, usar la estrategia existente, frecuentemente `Long id` con `GenerationType.IDENTITY`. `@Builder` y `@Builder.Default boolean estado = true` solo si aportan valor y el modelo realmente usa ese estado; no imponerlos a todos los registros.
- **DTO:** para CRUD simples, `[Entidad]DTOs` puede contener `record Request(...)` y `record Response(...)`. Aplicar las validaciones Jakarta necesarias, sin copiar ciegamente todos los campos de la entidad.
- **Mapper:** `@Mapper(componentModel = "spring")`. Ignorar en la conversión de creación `id`, códigos generados y campos administrados por el servidor **solo cuando existan y corresponda**. La antigua expresión `request.estado() != null ? request.estado() : true` es específica de DTO que admiten ese campo y ese valor predeterminado, no una regla universal. No sobrescribir datos inesperadamente en actualizaciones parciales.
- **Repositorio:** `JpaRepository<EntidadEntity, Long>` cuando sea JPA, usando métodos derivados como `findByNombreIgnoreCase`, `ContainingIgnoreCase` o `findByEstado` únicamente si son pertinentes al modelo.
- **Servicio:** métodos habituales `crear`, `obtenerPorId`, `obtenerTodos`, `buscarPorNombre`, `actualizar`, `cambiarEstado` solo si el proceso los necesita. Normalizar y validar antes de persistir; transaccionar operaciones que deban ser atómicas.
- **Controlador:** `@RestController`, `@RequiredArgsConstructor` y rutas bajo `/api/v1/...`, reutilizando los contratos existentes. Un CRUD típico puede tener `POST /` (201), `GET /`, `GET /buscar`, `GET /{id}`, `PUT /{id}` y `PATCH /{id}` para estado; **no generar automáticamente todos esos endpoints** si no son necesarios.
- **Prueba de servicio:** JUnit + Mockito con Given–When–Then. Cubrir creación, lectura, modificación, cambio de estado y situaciones de error que efectivamente existan.
- **Prueba de controlador:** `@WebMvcTest`, `MockMvc`, `ObjectMapper` y `@MockitoBean` según el proyecto. Comprobar rutas, estados y JSON; contemplar validaciones y seguridad donde correspondan.

## 6. Buenas prácticas Java y organización de métodos

- Respetar Java 21, tipado explícito y las convenciones existentes del módulo. Evitar clases gigantes, métodos difíciles de mantener y abstracciones prematuras.
- Mantener cada capa en su responsabilidad: Controller HTTP, Service negocio, Repository datos, Mapper conversiones y DTO contratos.
- Utilizar inyección por constructor, aprovechando `@RequiredArgsConstructor` si el proyecto ya utiliza Lombok.
- No añadir interfaces de Service artificiales ni capas extra solo por costumbre.
- Evitar `null` como éxito ficticio, `catch` que silencien excepciones, variables sin uso y logs de depuración permanentes.
- Reutilizar constantes, normalizadores y validaciones compartidas; evitar copiar reglas a varias clases.
- Usar comentarios para **separar bloques cuando el archivo sea grande** y para explicar decisiones no evidentes, no para describir cada línea.

Orden sugerido dentro de un Service o clase de negocio, si corresponde:

1. Dependencias inyectadas, constantes y propiedades relacionadas.
2. Inicialización u operaciones de ciclo de vida, si existen.
3. `crear` / `guardar`.
4. `listar` / `obtener`.
5. `buscar`.
6. `actualizar`.
7. `cambiarEstado` / otras transiciones.
8. Validaciones y métodos auxiliares privados.

No forzar métodos CRUD inexistentes solo para completar ese orden. Agrupar variables donde tengan sentido y no reorganizar una clase funcional por una edición pequeña.

## 7. Persistencia, validación y lógica de negocio

### Entity / JPA

- Mantener las asociaciones, cardinalidades, columnas, nulabilidad y restricciones reales. No añadir cascadas, eliminación en cascada ni cambios de relación sin justificar.
- Evitar exponer entidades JPA como respuestas públicas cuando el módulo maneja DTO.
- Evitar serialización recursiva y accesos involuntarios a relaciones lazy.
- No aplicar un booleano `estado` genérico a entidades clínicas cuyo estado de negocio sea distinto.

### DTO y mapeo

- Validar entrada con `@Valid` y anotaciones Jakarta adecuadas (`@NotNull`, `@NotBlank`, `@Pattern`, `@Email`, `@Size`, etc.), sin desplazar al DTO todas las invariantes de negocio.
- Distinguir DTO de entrada y salida; nunca devolver hashes de contraseña, tokens ni campos sensibles innecesarios.
- Reutilizar MapStruct donde exista y comprobar que campos nuevos se mapeen correctamente.
- No suponer que alta, actualización completa y actualización parcial tienen la misma semántica.

### Repository

- Preferir consultas derivadas o consultas explícitas legibles. Evitar cargar todos los registros para filtrar en memoria si una consulta concreta es suficiente.
- Considerar índices, unicidad, paginación, N+1 y acceso concurrente cuando la funcionalidad lo justifique.
- Las validaciones de duplicados del Service no sustituyen una restricción de unicidad en la base de datos cuando debe existir.

### Service y transacciones

- Ubicar aquí las reglas de negocio, validaciones, normalización y coordinación de repositorios.
- Reutilizar `StringNormalizer.normalizarTexto` y `normalizarNullable` cuando sean apropiados. Evitar cambiar el significado de textos clínicos, códigos y datos donde los espacios, mayúsculas o signos sean relevantes.
- Para proveedores, conservar las validaciones específicas ya utilizadas cuando se modifique ese submódulo: NIT `CF` o el formato comprobado en el código, teléfono de ocho dígitos y distinción entre registros duplicados activos e inactivos. **No generalizar esas reglas a todas las entidades.**
- Aplicar `@Transactional` en escrituras que lo requieran y `@Transactional(readOnly = true)` en consultas apropiadas. Verificar rollback y consistencia ante fallos.
- No normalizar todo indiscriminadamente ni duplicar validaciones globales sin motivo.
- No usar `repository.count() + 1` como regla para generar códigos únicos nuevos: puede producir duplicados por eliminaciones y operaciones concurrentes. Si un código así ya existe y no forma parte de la tarea, no refactorizarlo automáticamente; mencionarlo si afecta al cambio.
- Para inventario, medicamentos, compras, devoluciones, cuentas y movimientos, considerar atomicidad y concurrencia al modificar la lógica. Evitar saldos o existencias parcialmente actualizados.
- No introducir reintentos automáticos de escrituras no idempotentes que puedan crear registros duplicados.

## 8. Controllers, REST y compatibilidad con Angular

- Conservar el prefijo `/api/v1` y los endpoints, nombres de propiedades, tipos y formatos de respuesta utilizados actualmente.
- Los Controllers deben validar la entrada, delegar y responder. Evitar lógica de negocio y consultas JPA directas.
- Respetar los códigos HTTP del contrato existente; como guía: 201 creado, 200/204 éxito, 400 entrada inválida, 401 sin autenticación, 403 sin permiso, 404 recurso inexistente, 409 conflicto y 500 fallo inesperado.
- No modificar rutas, solicitudes, respuestas o estados HTTP sin petición expresa. Cuando un cambio de contrato sea necesario, indicar su impacto esperado sobre el frontend.
- No inventar contratos del proyecto Angular ni afirmar que se probó la integración sin acceso real a ese proyecto o sus servicios.
- Mantener documentados los endpoints cuando el módulo ya use OpenAPI; evitar anotaciones excesivas que no añadan claridad.
- El frontend tiene un timeout HTTP predeterminado de **5 segundos**. Este límite del cliente **no implica cancelación de la transacción del servidor**. Evitar bloqueos innecesarios e informar cuando una escritura pueda haberse completado aun si el cliente dejó de esperar.
- En operaciones críticas, considerar mecanismos para detectar peticiones repetidas cuando el proceso requiera idempotencia.

## 9. Seguridad, permisos y manejo de errores

- Aplicar autenticación y autorización en el backend, no depender solo de restricciones del frontend.
- Reutilizar Spring Security, la infraestructura JWT y las reglas de roles, puestos y horarios existentes. No introducir un sistema paralelo de permisos.
- La configuración documentada permite `/api/v1/**` con `permitAll` durante desarrollo. **No asumir que es válida para producción ni ampliarla.** No cambiar toda la política de seguridad sin autorización; señalar riesgos al trabajar en endpoints sensibles.
- Reutilizar el `ApiExceptionHandler` existente. Ajustar respuestas según la naturaleza del problema: validación, permisos, autenticación, no encontrado, conflicto o error interno.
- No clasificar indiscriminadamente las excepciones `RuntimeException` como 400; los fallos inesperados deben tratarse como errores del servidor, sin revelar detalles internos.
- No ocultar fallos detrás de respuestas `200 OK`, `null` o mensajes de éxito falsos.
- No incluir contraseñas, claves JWT, credenciales de BD, tokens, hashes, datos personales o información clínica sensible en logs, trazas públicas, documentación o este archivo.
- Mantener secretos fuera del código fuente cuando sea posible y utilizar los mecanismos de configuración seguros del entorno; no mostrar ni copiar valores privados de `application.properties`.
- No deshabilitar autenticación, autorizaciones, validación ni seguridad solo para hacer pasar una compilación o prueba.

## 10. MySQL, SQL y cambios de esquema

- Motor: MySQL. Verificar tablas, relaciones, columnas y datos reales antes de modificar persistencia.
- La configuración existente puede utilizar `spring.jpa.hibernate.ddl-auto=update`. No asumir que elimina columnas ni aplica automáticamente todas las restricciones; no utilizarla como sustituto de una migración controlada.
- **Todo script SQL nuevo debe guardarse en `demo/src/main/resources/db/`**. No crear carpetas SQL paralelas.
- Usar el nombre `[submodulo]_[tipo].sql` cuando sea pertinente, con `tipo` como `check`, `migracion`, `sp` o `data`; respetar nombres históricos existentes.
- Incluir en cada script un encabezado SQL con **Propósito**, **Uso** y **Ejecución/precauciones** (por ejemplo, si solo debe ejecutarse una vez y en qué orden).
- Los scripts de constraints, migraciones, procedimientos o datos iniciales deben ser claros sobre requisitos y efectos. No ejecutarlos sobre una BD real por iniciativa propia.
- No borrar o reinicializar tablas ni datos, hacer `DROP`, alterar relaciones sensibles o ejecutar migraciones destructivas sin confirmación expresa.
- Cuando una operación abarque múltiples tablas, preservar la consistencia con transacciones y restricciones adecuadas.

## 11. Limpieza obligatoria de código obsoleto

Cuando el usuario solicite **corregir, reemplazar, rediseñar o retirar** algo, eliminar los restos que ya no sean útiles **dentro de los archivos afectados**:

- Imports, campos, variables, métodos, condiciones, logs y comentarios obsoletos.
- DTO, mappings, consultas, validaciones y fragmentos duplicados que hayan quedado reemplazados.
- Código temporal de pruebas manuales que ya no corresponda.
- Archivos antiguos únicamente si su falta de uso está comprobada y el usuario autorizó el alcance correspondiente.

No dejar implementaciones anteriores comentadas «por si acaso» ni crear versiones paralelas sin uso. No eliminar servicios, utilidades, modelos ni código compartido que todavía utilicen otros módulos. Si no puede comprobarse que algo es innecesario, conservarlo e informar del posible residuo. **Esta limpieza es local: no iniciar una auditoría global por una modificación pequeña.**

## 12. Pruebas, comprobaciones y auditoría eficiente

### Elección de comprobaciones

- **Nivel 1:** lectura del diff y revisión puntual de referencias; sin compilación ni pruebas completas por defecto.
- **Nivel 2:** `compileJava` o pruebas focalizadas según lo que haya cambiado; actualizar pruebas si cambió el comportamiento.
- **Nivel 3:** pruebas relevantes de servicios/controladores, validación de contratos y, cuando corresponda, `test` o `build` completos.
- No ejecutar `bootRun` ni otros procesos persistentes como verificación puntual. Evitar comandos innecesarios en bucle.
- Si una prueba falla, analizar primero el error; corregir solo lo relacionado con la tarea y volver a ejecutar las comprobaciones afectadas.
- Distinguir fallos preexistentes de los provocados por los cambios. No cambiar aserciones únicamente para obtener resultados verdes.
- No declarar que una prueba se ejecutó si solo se inspeccionó el código.

### Pruebas por capa

- **ServiceTest:** JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`) cuando la arquitectura de la clase lo permita. Estilo Given–When–Then. Comprobar éxito, duplicados, recursos no encontrados, actualización y cambio de estado **solo si tales acciones existen**.
- **ControllerTest:** Spring MVC test (`@WebMvcTest`, `MockMvc`, `ObjectMapper` y `@MockitoBean`, si corresponde). Verificar método/ruta, JSON, códigos 201/200/400/401/403/404/409 según contrato y reglas de seguridad aplicables.
- **Persistencia/transacciones:** cuando sea relevante, añadir pruebas de integración o comprobaciones específicas; no asumir que Mockito prueba consistencia transaccional en MySQL.
- No crear pruebas ceremoniales por un renombrado o ajuste de comentario; tampoco omitir pruebas de negocio crítico por considerarlas demasiadas.
- Los tests unitarios/slice existentes suelen poder ejecutarse sin MySQL; verificar dependencias antes de ejecutar tests que levanten el contexto completo.

### Auditoría final (dentro del alcance)

Revisar errores de comportamiento, contratos, concurrencia, validaciones, seguridad, estados inconsistentes, duplicaciones, código inalcanzable, importaciones sin uso y tratamiento incorrecto de excepciones. Corregir lo que esté dentro del alcance y repetir solo las comprobaciones afectadas.

## 13. Reporte final proporcional al trabajo

**Modificación pequeña:** una o dos frases con el cambio y archivos afectados, más algún impedimento relevante. No presentar tablas extensas ni listados de pruebas que no fueron necesarios.

**Cambio funcional o grande:** informar brevemente:

1. **Implementado:** qué se modificó y qué comportamiento tiene.
2. **Reutilizado y limpiado:** componentes existentes aprovechados y residuos retirados.
3. **Comprobado:** comandos o pruebas realmente ejecutados y resultado; distinguir inspección de ejecución.
4. **Pendiente/riesgos:** endpoints o datos no disponibles, errores preexistentes, cambio de contratos y validaciones que requieran confirmación.

Nunca afirmar «todo funciona» solo porque el código compila. La entrega debe cumplir lo solicitado, respetar la arquitectura, preservar los datos y dejar el alcance afectado sin código fantasma.
