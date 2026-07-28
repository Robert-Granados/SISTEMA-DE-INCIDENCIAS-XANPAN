# Evidencia de TDD y trazabilidad de pruebas

## Ciclo 1: una incidencia no finaliza sin solucion

- **RED - commit `fca3510`:** `shouldRejectFinalizationThroughTransitionWithoutSolution` y `completesFullWorkflowOnlyAfterRegisteringSolution` fallaron porque `transitionState` permitia `EN_VALIDACION -> FINALIZADA` sin solucion.
- **GREEN - commit `bfb059d`:** `IncidentService` rechaza ese camino cuando `solutionDescription` es nula o vacia. El flujo valido usa `completeIncident`.
- **REFACTOR:** la validacion queda junto a las demas precondiciones de transicion, sin modificar el validador puro de la maquina de estados.

## Ciclo 2: limite EXPEDITE durante todo el ciclo de vida

- **RED - commit `fca3510`:** la prueba funcional marco dos criticas como EXPEDITE en `REGISTRADA`; despues ambas podian entrar a `EN_DESARROLLO`.
- **GREEN - commit `bfb059d`:** `ExpeditePolicy.canEnterActiveState` comprueba el repositorio al intentar entrar a `EN_DESARROLLO` o `EN_VALIDACION`.
- **REFACTOR:** `ExpediteService` e `IncidentService` comparten la misma politica; se elimino la busqueda duplicada del servicio de marcado.

## Ciclo 3: metricas basadas en la fecha real de cierre

- **Problema:** HU-05 contaba todos los cierres como throughput y usaba `updatedAt`; una edicion posterior alteraba artificialmente el lead time.
- **RED - commit `dcf8796`:** tres pruebas exigieron `closedAt`, un periodo `[inicio, fin)` y un reloj controlable. La suite fallo por las API todavia inexistentes.
- **GREEN - commit `4ebf823`:** `Incident` registra el cierre, `MetricsService` filtra por periodo y calcula desde `createdAt` hasta `closedAt`.
- **REFACTOR:** se inyecto `Clock`, se mantuvieron constructores compatibles y `touch()` concentro la actualizacion de `updatedAt`.
- **Pruebas de proteccion:** `shouldRecordClosureAndMeasureThroughputInsideRequestedPeriod`, `shouldUseClosureDateInsteadOfLaterUpdatesForAverageLeadTime` y `shouldRejectInvalidThroughputPeriods`.
- **Resultado:** `mvn clean verify` ejecuto 91 pruebas, sin fallos ni errores, y genero el JAR ejecutable.

## Refactorizacion demostrable

| Elemento | Evidencia |
| --- | --- |
| Situacion inicial | La regla EXPEDITE estaba duplicada y los tiempos dependian directamente de `LocalDateTime.now()` |
| Mejora aplicada | `ExpeditePolicy`, `Clock`, `closedAt` y `touch()` separan politica, tiempo de cierre y actualizaciones |
| Comportamiento protegido | Pruebas unitarias, dos pruebas funcionales y los ciclos RED/GREEN anteriores |
| Resultado verificable | 91 pruebas verdes; API anterior compatible; commits pequenos y reversibles |

## Dos pruebas funcionales

| Prueba | Flujo cubierto |
| --- | --- |
| `completesFullWorkflowOnlyAfterRegisteringSolution` | Registro, calculo critico, marcado EXPEDITE, cuatro estados y cierre con solucion |
| `registersFiltersAndMetricsWhileProtectingSingleActiveExpedite` | Dos registros, filtros, metricas, limite EXPEDITE, cierre y liberacion del cupo |

## Pruebas unitarias lideradas por Persona 2

| Responsabilidad | Evidencia |
| --- | --- |
| Identificador unico | `IncidentRegistrationTest.shouldGenerateUniqueId` |
| Prioridad critica | `PriorityCalculatorTest.shouldReturnCriticaWhenHighImpactAndHighUrgency` |
| Prioridad alta por urgencia | Casos de impacto medio/bajo y urgencia alta en `PriorityCalculatorTest` |
| Transiciones validas | `StateTransitionValidatorTest` |
| Cierre sin solucion | `IncidentServiceTest` y prueba funcional de flujo completo |
| Dos EXPEDITE activas | `ExpediteServiceTest` y prueba funcional de capacidad |
| Busquedas, vacios e inexistentes | `IncidentQueryTest` |
| Datos de metricas y casos vacios | `MetricsServiceTest` |

## Comandos de verificacion

```bash
# Suite Java completa
mvn clean verify

# Solo pruebas funcionales
mvn -Dgroups=functional test

# Esquema y restricciones PostgreSQL
docker compose up -d --wait
docker cp database/tests/schema_tests.sql xanpan-postgres:/tmp/schema_tests.sql
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U xanpan -d xanpan -f /tmp/schema_tests.sql
docker compose down --volumes
```
