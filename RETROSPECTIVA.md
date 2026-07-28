# Retrospectiva del incremento de Persona 2

Esta retrospectiva responde los ocho puntos solicitados y distingue los logros tecnicos de las limitaciones reales de trazabilidad encontradas durante la entrega.

## 1. ?Que aporto Kanban al trabajo de la pareja?

Kanban hizo visible la secuencia logica del producto: primero preparar criterios, despues escribir la prueba RED, implementar GREEN, validar y cerrar. Las politicas de entrada y salida evitaron tratar una historia como terminada solo porque compilaba. Tambien permitio relacionar HU-01 a HU-06 con sus pruebas y separar las tareas de CI, documentacion y demostracion. Sin embargo, el tablero externo no se mantuvo desde el primer dia; reconstruirlo al cierre no sustituye un historial progresivo y queda reconocido como una debilidad de evidencia.

## 2. ?Que dificultad genero el limite WIP?

El limite de una tarjeta en desarrollo obligo a terminar la validacion y publicar un commit pequeno antes de iniciar otra correccion. Esto hizo mas lento cambiar de contexto cuando aparecieron varios faltantes a la vez, especialmente ejecutable, metricas y documentos. A cambio, redujo cambios mezclados. Las tareas documentales tambien cuentan como trabajo activo: abrir otra historia mientras la anterior seguia en validacion habria ocultado capacidad real.

## 3. ?Que errores fueron detectados mediante TDD?

Las pruebas detectaron que `transitionState` permitia finalizar sin una solucion, que dos incidencias marcadas EXPEDITE podian entrar luego a estados activos y superar el limite, y que HU-05 calculaba lead time con `updatedAt` en vez de una fecha de cierre. Tambien revelaron que throughput contaba todos los cierres sin aceptar un periodo. Los commits `fca3510`/`bfb059d` y `dcf8796`/`4ebf823` conservan los ciclos RED y GREEN.

## 4. ?Que parte del codigo fue refactorizada?

Se extrajo `ExpeditePolicy` para que `ExpediteService` e `IncidentService` compartieran una sola regla de capacidad. Luego se incorporo un `Clock` inyectable al modelo y al servicio, se agrego `closedAt` y se centralizo la actualizacion temporal con `touch()`. Las interfaces existentes conservaron constructores compatibles y la regresion completa quedo en 91 pruebas verdes.

## 5. ?Como afecto el cambio de requerimiento?

EXPEDITE obligo a revisar el flujo completo, no solo a agregar una bandera. La prioridad debe ser critica, solo una EXPEDITE puede consumir capacidad activa y el cupo debe liberarse al finalizar. Por eso la regla se verifica tanto al marcar como al entrar en `EN_DESARROLLO` o `EN_VALIDACION`, manteniendo sin cambios el comportamiento de incidencias normales.

## 6. ?En que ayudo la IA?

La IA ayudo a auditar la implementacion recibida, localizar criterios incompletos, proponer casos limite, estructurar la demostracion ejecutable y comparar los entregables contra la rubrica del PDF. Cada propuesta conservada se comprobo con Maven, ejecucion del JAR, historial Git o CI. La bitacora registra objetivos, resultados, verificaciones y decisiones humanas.

## 7. ?En que se equivoco o fue insuficiente la IA?

La primera propuesta de EXPEDITE comprobaba el limite solamente al marcar, lo que dejaba un bypass durante las transiciones. Tambien resulto insuficiente considerar `docs/KANBAN.md` equivalente a un tablero externo con movimiento progresivo. Ambas ideas fueron modificadas; se rechazo agregar infraestructura PostgreSQL a pruebas funcionales que debian permanecer rapidas y deterministas.

## 8. ?Que cambiariamos en una siguiente version?

Creariamos el GitHub Project y sus tarjetas antes del primer cambio, registrariamos movimientos durante cada ciclo y coordinariamos al menos un commit significativo por persona y por dia. Tambien dejariamos Java 17+, Docker y la CLI configurados desde el inicio, conectariamos un repositorio PostgreSQL sin romper el contrato en memoria y documentariamos la revision cruzada de la pareja antes de pasar cada tarjeta a Hecho.
