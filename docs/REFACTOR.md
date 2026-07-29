# Evidencia de refactorizacion

## Objetivo

Las refactorizaciones buscaron reducir responsabilidades duplicadas y hacer
deterministas las metricas, manteniendo el comportamiento protegido por
pruebas. No se reescribio el sistema ni se cambiaron las reglas funcionales.

## Refactorizacion 1: centralizar la politica EXPEDITE

### Problema inicial

La comprobacion de EXPEDITE estaba concentrada en la accion de marcado. Dos
incidencias criticas podian marcarse mientras estaban REGISTRADA y entrar
despues a estados activos, superando el limite de una EXPEDITE en
EN_DESARROLLO o EN_VALIDACION.

Ademas, comprobar la misma regla desde servicios distintos podia producir
condiciones duplicadas y resultados inconsistentes.

### Cambio aplicado

Se creo `ExpeditePolicy` como unica definicion de:

- que incidencias pueden marcarse como EXPEDITE;
- que estados consumen el cupo activo;
- si otra incidencia EXPEDITE ya ocupa ese cupo.

`ExpediteService` usa la politica al marcar y `IncidentService` la reutiliza
al intentar entrar en EN_DESARROLLO o EN_VALIDACION.

### Comportamiento protegido

- una incidencia no critica es rechazada;
- una critica puede marcarse;
- dos EXPEDITE pueden estar registradas o listas;
- solamente una puede permanecer en un estado activo;
- finalizar libera el cupo;
- las incidencias normales conservan su flujo.

### Evidencia Git

- RED: `fca3510` — pruebas que muestran el cierre inseguro y dos EXPEDITE
  activas.
- GREEN/REFACTOR: `bfb059d` — politica compartida y validacion durante las
  transiciones.

## Refactorizacion 2: separar cierre real de ultima actualizacion

### Problema inicial

El lead time utilizaba `updatedAt`. Si una incidencia finalizada se editaba,
la metrica cambiaba aunque el momento de cierre siguiera siendo el mismo.
Throughput tampoco aceptaba un periodo y contaba todos los cierres historicos.

Las llamadas directas a `LocalDateTime.now()` hacian dificil probar tiempos
exactos sin esperas.

### Cambio aplicado

- se agrego `closedAt` para conservar el instante real de cierre;
- se inyecto `Clock` en `Incident` e `IncidentService`;
- se extrajo `touch()` para centralizar la actualizacion de `updatedAt`;
- `MetricsService` calcula lead time entre `createdAt` y `closedAt`;
- throughput acepta el intervalo semiabierto `[inicio, fin)`.

Los constructores anteriores se conservaron para no romper a sus consumidores.

### Comportamiento protegido

- cerrar registra una fecha de cierre;
- editar despues del cierre no cambia el lead time;
- solo se cuentan cierres dentro del periodo solicitado;
- periodos nulos o invertidos son rechazados;
- el caso sin incidencias mantiene metricas en cero.

### Evidencia Git

- RED: `dcf8796` — pruebas para fecha de cierre, periodo y reloj controlable.
- GREEN/REFACTOR: `4ebf823` — `Clock`, `closedAt`, `touch()` y calculos
  corregidos.

## Refactorizacion 3: separar la interfaz de las reglas del dominio

### Problema evitado

Al agregar una interfaz grafica era posible duplicar dentro de la ventana las
reglas de prioridad, estados y EXPEDITE. Eso habria creado dos lugares con el
mismo comportamiento y dificultado las pruebas.

### Cambio aplicado

`IncidentFrame` se limita a mostrar datos y capturar acciones.
`IncidentController` coordina los servicios existentes y no importa ninguna
clase Swing. La ventana no accede directamente al repositorio ni modifica una
incidencia por su cuenta.

### Comportamiento protegido

`IncidentControllerTest` verifica registro, prioridad, filtros, transiciones,
cierre, EXPEDITE y metricas sin abrir una ventana. `IncidentTableModelTest`
protege la representacion de la tabla.

### Evidencia Git

- Implementacion y pruebas: `80a724e`.
- Guia de arquitectura y demostracion: `781402a`.

## Resultado verificable

La verificacion actual ejecuta:

```bash
mvn clean verify
```

Resultado observado:

```text
Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

GitHub Actions tambien valida la suite Java y el esquema PostgreSQL en cada
`push` y `pull_request`.

## Explicacion corta para la defensa

> Primero eliminamos la duplicacion de la regla EXPEDITE mediante una politica
> compartida. Despues separamos la fecha real de cierre de la ultima
> actualizacion e inyectamos un reloj para probar las metricas. Finalmente,
> mantuvimos la interfaz grafica fuera del dominio mediante un controlador.
> En los tres casos conservamos las pruebas verdes y las API existentes.
