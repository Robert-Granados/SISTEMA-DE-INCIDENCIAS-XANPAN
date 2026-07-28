# Tablero Kanban y politicas de trabajo

Este archivo es la fuente versionada del tablero del proyecto. Cada cambio de estado debe actualizarse en el mismo commit que aporta su evidencia tecnica.

## Columnas y limites WIP

| Columna | Politica de entrada | Limite WIP |
| --- | --- | ---: |
| Backlog | Historia identificada, todavia sin preparar | Sin limite |
| Preparado | Criterios de aceptacion claros y dependencias resueltas | 3 |
| En desarrollo | Existe una prueba RED o una tarea tecnica activa | 1 para toda la pareja |
| Validacion | Implementacion GREEN, pendiente de revision/regresion | 1 |
| Hecho | Cumple la Definition of Done | Sin limite |

Las actividades de documentacion no habilitan el desarrollo simultaneo de otra historia. EXPEDITE cambia el orden de atencion, pero no elimina el limite WIP.

## Definition of Ready

Una tarjeta puede entrar en Preparado cuando:

- expresa valor, alcance y criterios de aceptacion verificables;
- identifica pruebas esperadas y dependencias;
- cabe en un incremento pequeno;
- ambos integrantes pueden explicar su objetivo.

## Definition of Done

Una tarjeta pasa a Hecho cuando:

- todos sus criterios estan cubiertos por pruebas;
- `mvn clean verify` termina correctamente;
- las pruebas SQL terminan correctamente cuando afecta persistencia;
- no rompe historias anteriores;
- recibio revision cruzada;
- documentacion y evidencia TDD estan actualizadas;
- CI queda verde en la rama publicada.

## Tarjetas

| ID | Tarjeta | Criterio principal | Responsable inicial | Estado | Evidencia |
| --- | --- | --- | --- | --- | --- |
| HU-01 | Registrar incidencia | Validaciones e identificador unico | Pareja; Persona 2 prueba ID | Hecho | `IncidentRegistrationTest` |
| HU-02 | Calcular prioridad | Matriz impacto/urgencia y nulos | Persona 2 inicia TDD | Hecho | `PriorityCalculatorTest` |
| HU-03 | Estados y transiciones | Sin saltos, retrocesos ni cierre sin solucion | Persona 2 pruebas | Hecho | `StateTransitionValidatorTest`, `IncidentServiceTest` |
| HU-04 | Consultas y filtros | ID, estado, prioridad, abiertas y finalizadas | Persona 2 pruebas | Hecho | `IncidentQueryTest` |
| HU-05 | Metricas | Totales, throughput, lead time y prioridad | Persona 2 pruebas | Hecho | `MetricsServiceTest` |
| HU-06 | Cambio EXPEDITE | Solo critica y una activa | Persona 2 driver inicial | Hecho | `ExpediteServiceTest`, pruebas funcionales |
| TK-CI | Integracion continua | Compilar, probar y fallar en push/PR | Persona 2 | Hecho | `.github/workflows/ci.yml` |
| TK-DOC | IA y retrospectiva | Uso, rechazo, verificacion y aprendizaje | Persona 2 | Hecho | `IA-LOG.md`, `RETROSPECTIVA.md` |

## Criterios verificables de HU-06 EXPEDITE

1. Solo una incidencia con prioridad `CRITICA` puede marcarse EXPEDITE.
2. Una incidencia no critica es rechazada.
3. No pueden coexistir dos EXPEDITE en `EN_DESARROLLO` o `EN_VALIDACION`.
4. El limite se comprueba tanto al marcar como al entrar en un estado activo.
5. Las EXPEDITE en `REGISTRADA`, `LISTA` o `FINALIZADA` no consumen el cupo activo.
6. Las incidencias normales conservan su comportamiento.

## Politica de ramas y revision

- Rama principal protegida por CI: `main`.
- Ramas de trabajo: `feature/<nombre>`.
- Pull requests pequenos, con criterios y pruebas enlazados.
- La persona que no actuo como Driver revisa la tarjeta en Validacion.
- No se mueve a Hecho con pruebas fallidas o revision pendiente.
