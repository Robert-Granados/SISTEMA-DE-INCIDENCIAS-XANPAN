# Retrospectiva del incremento de Persona 2

## Objetivo del incremento

Completar la responsabilidad de Persona 2 sobre pruebas automatizadas, transiciones, EXPEDITE, integracion continua, tablero Kanban y documentacion, tomando como base la arquitectura de Persona 1.

## Que salio bien

- La implementacion de Persona 1 pudo integrarse sin reescritura y sus 85 pruebas iniciales quedaron como red de regresion.
- Las pruebas funcionales revelaron dos bypasses que las pruebas unitarias aisladas no cubrian: finalizar sin solucion y activar dos EXPEDITE marcadas previamente.
- La politica EXPEDITE quedo separada de los servicios, con una unica definicion de que estados consumen el limite activo.
- El trabajo se dividio en commits pequenos: integracion de la base, evidencia RED, solucion GREEN y documentacion/CI.
- Java y PostgreSQL tienen validaciones complementarias: el dominio protege el comportamiento de la aplicacion y el indice unico protege la concurrencia en la base de datos.

## Que dificulto el trabajo

- La terminal usaba Java 11 aunque el proyecto exige Java 17 o superior. La verificacion local necesito seleccionar explicitamente el JDK 23 instalado.
- La cache global de Maven no era escribible en el entorno. Se utilizo una cache local ignorada por Git.
- El tablero remoto de GitHub no se pudo administrar desde la terminal porque la CLI `gh` no esta instalada y la conexion disponible no expone operaciones de GitHub Projects. Se dejo el tablero completo y auditable en `docs/KANBAN.md`.

## Que aprendimos

- Una regla de capacidad debe comprobarse cuando se consume la capacidad, no solamente cuando se etiqueta una entidad.
- Un camino alternativo de API puede invalidar una regla aunque el flujo principal esta bien probado.
- Las pruebas funcionales pequenas, apoyadas en dobles en memoria, encuentran errores de coordinacion sin volver lenta la suite.
- Mantener la evidencia RED y GREEN en commits consecutivos hace verificable el uso de TDD.

## Acciones para el siguiente incremento

- Conectar `IncidentRepository` a PostgreSQL manteniendo el contrato probado por el repositorio en memoria.
- Incorporar un reloj inyectable para probar lead time con valores exactos y sin depender de `LocalDateTime.now()`.
- Materializar `docs/KANBAN.md` como GitHub Project cuando exista acceso a Projects y enlazar cada tarjeta con su issue o pull request.
- Configurar Java 17+ como version predeterminada del entorno de desarrollo.

## Start / Stop / Continue

- **Start:** probar reglas de capacidad en todas las transiciones que cambian el consumo de WIP.
- **Stop:** asumir que una validacion en un solo servicio protege todos los caminos publicos.
- **Continue:** TDD, commits pequenos, revision cruzada y regresion completa antes de mover una tarjeta a Hecho.
