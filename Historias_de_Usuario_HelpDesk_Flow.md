# Historias de Usuario — HelpDesk Flow

Proyecto: Sistema de gestión de incidencias técnicas (Xanpan / XP / Kanban con IA)
Curso: ITI-822 — Metodologías Ágiles de Desarrollo de Software

---

## HU-01. Registrar una incidencia

**Como** usuario, **deseo** registrar una incidencia indicando su título, descripción, impacto, urgencia y categoría, **para que** el equipo técnico pueda atenderla.

### Criterios de aceptación
- El título no puede estar vacío.
- La descripción debe contener al menos diez caracteres.
- El impacto debe ser `BAJO`, `MEDIO` o `ALTO`.
- La urgencia debe ser `BAJA`, `MEDIA` o `ALTA`.
- El sistema debe generar un identificador único.

---

## HU-02. Calcular automáticamente la prioridad

**Como** encargado de soporte, **deseo** que el sistema calcule la prioridad de la incidencia **para** evitar decisiones arbitrarias.

### Reglas de cálculo

| Impacto | Urgencia | Prioridad |
|---|---|---|
| Alto | Alta | Crítica |
| Alto | Media o baja | Alta |
| Medio o bajo | Alta | Alta |
| Cualquier otra combinación | Cualquier otra combinación | Normal |

---

## HU-03. Gestionar el flujo de la incidencia

**Como** técnico, **deseo** cambiar el estado de una incidencia **para** representar su avance real.

### Estados permitidos
```
REGISTRADA → LISTA → EN_DESARROLLO → EN_VALIDACION → FINALIZADA
```

### Restricciones
- El sistema debe impedir saltos o retrocesos no autorizados entre estados.
- `REGISTRADA → FINALIZADA` es una transición inválida.
- `FINALIZADA → EN_DESARROLLO` es una transición inválida.
- Una incidencia no puede pasar a `FINALIZADA` si no posee una descripción de la solución aplicada.

---

## HU-04. Consultar y filtrar incidencias

**Como** usuario del sistema, **deseo** consultar y filtrar incidencias **para** ubicar información relevante de forma rápida.

### El sistema debe permitir
- Mostrar todas las incidencias.
- Buscar una incidencia por identificador.
- Filtrar por estado.
- Filtrar por prioridad.
- Mostrar solamente incidencias abiertas.
- Mostrar solamente incidencias finalizadas.

---

## HU-05. Generar métricas básicas

**Como** encargado de soporte o gestor del flujo, **deseo** ver métricas básicas del sistema **para** evaluar el desempeño del equipo.

### El sistema deberá mostrar
- Cantidad total de incidencias.
- Cantidad de incidencias finalizadas.
- Cantidad de incidencias abiertas.
- Throughput: incidencias terminadas durante el periodo registrado.
- Lead time promedio de las incidencias terminadas.
- Cantidad de incidencias por prioridad.

---

## HU-06. Cambio de requerimiento — Clase de servicio EXPEDITE

**Como** organización, **deseo** incorporar una clase de servicio EXPEDITE **para** que las incidencias críticas urgentes reciban atención prioritaria.

### Descripción
Una incidencia crítica podrá marcarse como urgente (EXPEDITE) y recibir atención prioritaria. Solamente podrá existir **una** incidencia EXPEDITE en desarrollo o validación de forma simultánea.

### Pasos obligatorios para incorporar el cambio
1. Crear una nueva tarjeta en el backlog.
2. Definir criterios de aceptación verificables.
3. Escribir primero las pruebas asociadas al cambio (TDD).
4. Adaptar el diseño existente sin eliminar el comportamiento previamente validado.
5. Mantener en funcionamiento todas las pruebas anteriores.
6. Integrar el cambio sin reescribir innecesariamente el sistema.

### Criterios de aceptación sugeridos
- Solo se puede marcar como EXPEDITE una incidencia con prioridad `Crítica`.
- El sistema debe rechazar marcar una segunda incidencia como EXPEDITE si ya existe una en `EN_DESARROLLO` o `EN_VALIDACION`.
- Una incidencia EXPEDITE debe ser identificable en las consultas/listados (HU-04).
- El comportamiento previo de las demás incidencias (no EXPEDITE) no debe verse alterado.

---

## Resumen de trazabilidad

| Historia | Relacionada con |
|---|---|
| HU-01 | Registro de incidencias, validaciones |
| HU-02 | Cálculo automático de prioridad |
| HU-03 | Máquina de estados / transiciones válidas |
| HU-04 | Consultas y filtros |
| HU-05 | Métricas (throughput, lead time) |
| HU-06 | Cambio de requerimiento EXPEDITE (obligatorio, con TDD) |

> Nota: Cada historia debe convertirse en una o más tarjetas en el tablero Kanban, respetando las columnas (Backlog, Preparado, En desarrollo, Validación, Hecho) y los límites WIP definidos en el enunciado (Preparado: 3, En desarrollo: 1 por pareja, Validación: 1).
