# Bitacora de uso de IA

## Contexto

- Fecha: 28 de julio de 2026.
- Rama de trabajo: `feature/Alejandro`.
- Alcance: pruebas automatizadas, estados, EXPEDITE, integracion continua, Kanban y documentacion de la Persona 2.
- Herramienta: Codex, utilizada como apoyo tecnico. Todas las propuestas se revisaron y verificaron antes de conservarlas.

## Registro estructurado de interacciones

| Fecha | Herramienta | Objetivo | Resultado de la IA | Verificacion | Cambio o decision humana |
| --- | --- | --- | --- | --- | --- |
| 2026-07-28 | Codex | Auditar la base de Persona 1 antes de integrar | Identifico servicios, repositorio y 85 pruebas reutilizables | `mvn clean verify` sobre la base | Se conservo la autoria de Robert y se integro sin reescribir |
| 2026-07-28 | Codex | Cubrir cierre seguro y limite EXPEDITE con TDD | Propuso pruebas funcionales y una politica compartida | Commit RED `fca3510`, commit GREEN `bfb059d` y 87 pruebas verdes | Se amplio la comprobacion a cada entrada en estado activo |
| 2026-07-28 | Codex | Convertir el proyecto en una entrega ejecutable | Propuso una demostracion de consola y manifiesto del JAR | Commit `d4d3e50`, 88 pruebas y ejecucion de `java -jar` | Se eligio una demo determinista sin framework adicional |
| 2026-07-28 | Codex | Corregir throughput y lead time segun HU-05 | Detecto que faltaba periodo y que `updatedAt` no representa el cierre | RED `dcf8796`, GREEN `4ebf823` y 91 pruebas verdes | Se inyecto `Clock`, se agrego `closedAt` y se mantuvo la API anterior compatible |

## Respuesta utilizada y modificada

La IA propuso revisar la implementacion de Persona 1 antes de agregar codigo. La respuesta se utilizo para integrar el commit `6f19e55` como base, conservando su autoria, y ejecutar primero las 85 pruebas existentes.

La propuesta inicial comprobaba el limite EXPEDITE unicamente al llamar a `markExpedited`. Se modifico porque ese control no cubria este escenario: dos incidencias podian marcarse mientras estaban `REGISTRADA` y despues ambas podian entrar en `EN_DESARROLLO`. La solucion final centraliza la regla en `ExpeditePolicy` y tambien la aplica durante cada transicion a un estado activo.

## Sugerencia rechazada

Se rechazo agregar PostgreSQL o una libreria de persistencia a las pruebas funcionales Java. La razon tecnica es que los dos flujos funcionales validan coordinacion entre dominio, repositorio, servicios, filtros y metricas sin necesitar infraestructura externa. Mantener `InMemoryIncidentRepository` hace esas pruebas deterministas y rapidas; las restricciones fisicas de PostgreSQL se verifican por separado con `database/tests/schema_tests.sql` en CI.

Tambien se rechazo permitir `transitionState(..., FINALIZADA)` sin una solucion y confiar solamente en `completeIncident`. Tener dos caminos publicos con reglas distintas dejaba un bypass del criterio de aceptacion de HU-03.

## Verificacion de la respuesta utilizada

### RED

Comando:

```bash
mvn -Dtest=IncidentServiceTest,IncidentWorkflowFunctionalTest test
```

Resultado observado antes del cambio de produccion: 14 pruebas ejecutadas y 3 fallos esperados. Los fallos demostraron el cierre sin solucion y la posibilidad de una segunda EXPEDITE activa.

### GREEN

Comando:

```bash
mvn -Dtest=IncidentServiceTest,ExpediteServiceTest,IncidentWorkflowFunctionalTest test
```

Resultado observado despues del cambio: 22 pruebas ejecutadas, 0 fallos y 0 errores.

### Regresion

La verificacion final se realiza con:

```bash
mvn clean verify
```

Resultado Java observado: 91 pruebas ejecutadas, 0 fallos y 0 errores.

La validacion SQL local no pudo iniciarse porque Docker Desktop no estaba ejecutandose. Debe confirmarse con el job PostgreSQL de CI al publicar la rama.

El pipeline de `.github/workflows/ci.yml` ejecuta la misma verificacion y, ademas, levanta PostgreSQL para ejecutar las pruebas del esquema SQL.

## Decisiones humanas conservadas

- Se mantuvo el modelo de dominio y los servicios creados por Persona 1.
- Se conservaron las interfaces existentes y se agregaron constructores compatibles para inyectar la politica y el reloj en pruebas.
- Se evito reescribir el sistema: los cambios se limitaron a la politica EXPEDITE, el cierre seguro, pruebas y documentacion.
