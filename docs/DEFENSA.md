# Guia de defensa del proyecto

## 1. Presentacion corta

> Nuestro proyecto es un sistema de gestion de incidencias desarrollado con
> Java 17, Maven y PostgreSQL. Permite registrar incidencias, calcular su
> prioridad, controlar un flujo de estados, aplicar la politica EXPEDITE,
> consultar y filtrar registros, y calcular metricas. Trabajamos con TDD,
> Kanban, integracion continua y uso documentado de IA.

Duracion recomendada de esta introduccion: 30 a 45 segundos.

## 2. Que debe estar preparado antes de iniciar

- JDK 17 o superior.
- Maven disponible.
- Rama `feature/Alejandro` actualizada.
- Terminal abierta en la raiz del proyecto.
- Navegador abierto en GitHub Actions y el tablero Kanban.
- La interfaz cerrada antes de compilar, para iniciar la demostracion desde
  cero.

En PowerShell:

```powershell
cd C:\Users\Usuario\Desktop\Metodo\SISTEMA-DE-INCIDENCIAS-XANPAN

$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

java -version
mvn -version
```

Si Maven muestra `release version 17 not supported`, significa que esta usando
un JDK anterior. Se corrige ejecutando nuevamente las dos instrucciones de
`JAVA_HOME` y `Path`.

## 3. Orden recomendado de la defensa

| Etapa | Tiempo | Responsable sugerido |
| --- | ---: | --- |
| Presentacion y objetivo | 1 minuto | Robert |
| Arquitectura y modelo | 2 minutos | Robert |
| Interfaz y reglas funcionales | 4 minutos | Alejandro |
| TDD y pruebas | 2 minutos | Alejandro |
| Refactorizacion | 1 minuto | Robert |
| Kanban, CI e IA | 2 minutos | Alejandro |
| Cierre y preguntas | restante | Ambos |

Los dos deben poder responder cualquier punto aunque exista un responsable
sugerido.

## 4. Demostracion tecnica paso a paso

### Paso 1: demostrar que compila y que las pruebas pasan

```powershell
mvn clean verify
```

Resultado esperado:

```text
Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Explicacion:

> `clean` elimina artefactos anteriores, `verify` compila el proyecto, ejecuta
> todas las pruebas y genera el JAR. Esto demuestra que el resultado no depende
> de una compilacion antigua.

### Paso 2: abrir la interfaz

```powershell
java -jar target\sistema-incidencias-1.0.0-SNAPSHOT.jar
```

La ventana tiene tres zonas:

1. Formulario de registro.
2. Tabla, busqueda, filtros y detalle.
3. Acciones de flujo y distribucion por prioridad.

En la parte superior aparecen total, abiertas, cerradas, throughput y lead
time.

### Paso 3: cargar un escenario rapido

Presionar `Cargar datos de ejemplo`.

Se crean:

- una incidencia critica de pagos marcada EXPEDITE;
- una incidencia normal de hardware;
- una incidencia de red.

Explicacion:

> El boton solamente prepara datos para la demostracion. Las incidencias pasan
> por los mismos servicios y reglas que un registro manual.

### Paso 4: demostrar prioridad y EXPEDITE

Seleccionar `Servicio de pagos no disponible`.

Mostrar:

- impacto ALTO;
- urgencia ALTA;
- prioridad CRITICA calculada automaticamente;
- marca EXPEDITE;
- estado EN DESARROLLO.

Explicacion:

> La interfaz no calcula la prioridad. Envia impacto y urgencia a
> `IncidentService`, que utiliza `PriorityCalculator`. EXPEDITE solo se permite
> para incidencias criticas y solamente una puede ocupar un estado activo.

### Paso 5: demostrar el flujo

Con la incidencia critica seleccionada:

1. Presionar `Avanzar estado`.
2. Confirmar que pasa de EN DESARROLLO a EN VALIDACION.
3. Observar que `Avanzar estado` queda deshabilitado.

Explicacion:

> El flujo permitido es REGISTRADA, LISTA, EN DESARROLLO, EN VALIDACION y
> FINALIZADA. No se permiten saltos ni retrocesos. El ultimo paso utiliza una
> operacion separada porque necesita una solucion.

### Paso 6: demostrar el cierre con solucion

1. Presionar `Finalizar con solucion`.
2. Escribir, por ejemplo:

```text
Se restauro la conexion con el proveedor de pagos.
```

3. Confirmar.
4. Mostrar estado FINALIZADA, fecha de cierre y solucion.

Explicacion:

> No se puede finalizar una incidencia sin solucion. `completeIncident`
> comprueba que este en EN VALIDACION, registra la solucion y conserva
> `closedAt` como fecha real de cierre.

### Paso 7: demostrar filtros y metricas

- Buscar por titulo o categoria.
- Filtrar por `Finalizadas`.
- Filtrar por prioridad `CRITICA`.
- Mostrar el cambio en abiertas, cerradas y throughput.

Explicacion:

> Throughput cuenta cierres y tambien dispone de un calculo por periodo. Lead
> time mide desde `createdAt` hasta `closedAt`, por lo que una edicion posterior
> no altera la metrica.

### Paso 8: mostrar que se conservo la consola

Cerrar la ventana y ejecutar:

```powershell
java -jar target\sistema-incidencias-1.0.0-SNAPSHOT.jar --console
```

Explicacion:

> La interfaz se agrego sin eliminar la demostracion original. El mismo JAR
> ofrece modo grafico y modo consola.

## 5. Arquitectura para explicar

```text
App
 |
 +-- IncidentFrame                Interfaz Swing
       |
       +-- IncidentController     Coordinacion de acciones
             |
             +-- IncidentService
             +-- ExpediteService
             +-- MetricsService
                    |
                    +-- IncidentRepository
                           |
                           +-- InMemoryIncidentRepository

Servicios auxiliares:

PriorityCalculator
StateTransitionValidator
ExpeditePolicy
```

### Responsabilidad de cada pieza

| Componente | Responsabilidad |
| --- | --- |
| `Incident` | Datos y estado de una incidencia |
| `PriorityCalculator` | Matriz de impacto y urgencia |
| `StateTransitionValidator` | Transiciones permitidas |
| `ExpeditePolicy` | Reglas y capacidad EXPEDITE |
| `IncidentService` | Registro, consultas y cambios de estado |
| `MetricsService` | Totales, throughput y lead time |
| `IncidentRepository` | Contrato de almacenamiento |
| `IncidentController` | Coordina los casos de uso de la interfaz |
| `IncidentFrame` | Presenta informacion y captura acciones |

Idea principal:

> La interfaz no contiene reglas de negocio. Si se reemplazara Swing por una
> API web, los servicios y las pruebas del dominio seguirian funcionando.

## 6. Reglas de negocio importantes

### Registro

- titulo obligatorio;
- descripcion de al menos diez caracteres;
- identificador unico;
- estado inicial REGISTRADA;
- prioridad calculada automaticamente.

### Prioridad

- impacto ALTO y urgencia ALTA producen CRITICA;
- combinaciones de riesgo intermedio producen ALTA;
- casos de menor impacto y urgencia producen NORMAL.

### Estados

```text
REGISTRADA
   |
   v
LISTA
   |
   v
EN_DESARROLLO
   |
   v
EN_VALIDACION
   |
   v
FINALIZADA
```

- no se permiten saltos;
- no se permiten retrocesos;
- FINALIZADA no puede reabrirse;
- el cierre exige una solucion.

### EXPEDITE

- solamente las incidencias con prioridad CRITICA pueden marcarse;
- una no critica es rechazada;
- solamente una EXPEDITE puede estar en EN_DESARROLLO o EN_VALIDACION;
- REGISTRADA, LISTA y FINALIZADA no consumen el cupo activo;
- finalizar libera el cupo.

## 7. Como explicar TDD

TDD se aplico como:

```text
RED -> GREEN -> REFACTOR
```

| Ciclo | RED | GREEN/REFACTOR |
| --- | --- | --- |
| Cierre y EXPEDITE | `fca3510` | `bfb059d` |
| Fecha de cierre y metricas | `dcf8796` | `4ebf823` |

Explicacion:

> Primero agregamos pruebas que fallaban por el comportamiento faltante. Luego
> implementamos lo minimo para dejarlas verdes y finalmente eliminamos
> duplicacion sin cambiar el resultado.

Ejemplos de errores descubiertos:

- `transitionState` podia finalizar sin solucion;
- dos incidencias EXPEDITE marcadas previamente podian entrar a estados
  activos;
- lead time utilizaba la ultima modificacion y no la fecha real de cierre;
- throughput no aceptaba un periodo.

La interfaz agrego pruebas del controlador sin abrir ventanas:

- `IncidentControllerTest`;
- `IncidentTableModelTest`;
- modo consola en `AppTest`.

## 8. Refactorizacion

Las tres mejoras principales estan documentadas en `docs/REFACTOR.md`.

### Politica EXPEDITE

Antes, la regla podia quedar duplicada entre servicios. Ahora
`ExpeditePolicy` es compartida por marcado y transiciones.

### Tiempo y metricas

Se agregaron `Clock`, `closedAt` y `touch()`. Esto separa:

- fecha de creacion;
- ultima actualizacion;
- fecha real de cierre.

### Interfaz

`IncidentFrame` contiene Swing y `IncidentController` coordina los servicios.
El controlador puede probarse sin interfaz grafica.

Frase corta:

> Refactorizar no fue agregar comportamiento; fue mejorar la estructura
> conservando todas las pruebas verdes.

## 9. Kanban

[Tablero HelpDesk Flow - Xanpan](https://github.com/users/AlejandroXV5/projects/1)

Columnas:

- Backlog;
- Preparado, WIP 3;
- En desarrollo, WIP 1;
- Validacion, WIP 1;
- Hecho.

Definition of Done:

- criterios cubiertos;
- pruebas verdes;
- regresion completa;
- documentacion actualizada;
- CI verde;
- revision antes de mover a Hecho.

Explicacion del WIP:

> El limite de una tarjeta en desarrollo obliga a terminar, validar y publicar
> antes de comenzar otra. EXPEDITE cambia prioridad de atencion, pero no elimina
> el limite.

## 10. Integracion continua

Workflow:

```text
.github/workflows/ci.yml
```

Se ejecuta en:

- cada `push`;
- cada `pull_request`.

Valida:

- Java 17;
- compilacion Maven;
- 99 pruebas Java;
- inicio de PostgreSQL;
- pruebas del esquema SQL;
- limpieza del servicio.

Evidencia controlada:

- los commits RED fallaron en GitHub Actions;
- los commits GREEN siguientes pasaron;
- el PR actualizado termina en verde.

## 11. Uso de inteligencia artificial

La evidencia esta en `IA-LOG.md`.

Uso correcto:

- se utilizo para auditar, proponer pruebas, detectar faltantes y estructurar la
  interfaz;
- las propuestas se revisaron antes de conservarlas;
- cada resultado se verifico con pruebas, ejecucion o CI;
- se documentaron respuestas modificadas y sugerencias rechazadas.

Ejemplo de modificacion:

> La propuesta inicial comprobaba EXPEDITE solamente al marcar. Se modifico
> para validar tambien cada entrada a un estado activo.

Ejemplo de rechazo:

> Se rechazo agregar PostgreSQL a las pruebas funcionales Java porque el
> repositorio en memoria mantiene esas pruebas rapidas y deterministas. El
> esquema PostgreSQL se prueba por separado en CI.

## 12. Division para la exposicion

### Robert

- objetivo y arquitectura;
- modelo `Incident`;
- repositorio y PostgreSQL;
- `PriorityCalculator`;
- refactorizacion.

### Alejandro

- interfaz grafica;
- estados y cierre;
- EXPEDITE;
- pruebas y TDD;
- Kanban, GitHub Actions e IA.

### Ambos

- deben comprender el flujo completo;
- deben poder ejecutar la aplicacion;
- deben explicar cualquier prueba;
- deben reconocer las limitaciones reales.

## 13. Preguntas probables y respuestas

### Por que usaron Java Swing

Porque forma parte del JDK, no requiere servidor ni dependencias adicionales y
permite demostrar el dominio mediante un JAR ejecutable.

### La interfaz guarda datos en PostgreSQL

No. Actualmente utiliza `InMemoryIncidentRepository`. PostgreSQL contiene el
esquema y sus restricciones, validados en CI. La interfaz `IncidentRepository`
permite agregar un adaptador PostgreSQL posteriormente sin modificar el
dominio.

### Que ocurre al cerrar la aplicacion

Los datos de la interfaz se pierden porque el repositorio esta en memoria. Es
una limitacion conocida de esta version demostrable.

### Por que no colocaron las reglas en la ventana

Para evitar duplicacion y permitir que consola, interfaz y pruebas compartan la
misma logica.

### Como impiden cerrar sin solucion

`completeIncident` exige estado EN_VALIDACION y una solucion no vacia. El camino
generico de transicion tambien rechaza FINALIZADA sin solucion.

### Como impiden dos EXPEDITE activas

`ExpeditePolicy` consulta el repositorio al marcar y al entrar a EN_DESARROLLO
o EN_VALIDACION.

### Diferencia entre throughput y lead time

- throughput: cantidad de incidencias finalizadas en un periodo;
- lead time: tiempo promedio desde creacion hasta cierre.

### Por que inyectaron Clock

Para controlar el tiempo en pruebas y evitar esperas o resultados variables.

### Como saben que la integracion continua detecta fallos

Los commits RED produjeron workflows fallidos y los GREEN inmediatamente
posteriores pasaron. El historial no fue reescrito.

### Que refactorizaron

Centralizamos EXPEDITE, separamos fecha de cierre de actualizacion y aislamos la
interfaz mediante un controlador.

### Que aporto Kanban

Hizo visible el trabajo y obligo a cerrar una tarjeta antes de abrir otra por
los limites WIP.

### Que aporto la IA

Ayudo a encontrar casos limite y organizar evidencia, pero sus propuestas
fueron modificadas o rechazadas cuando no respetaban las reglas o el diseno.

## 14. Limitaciones que deben reconocer

Responder con honestidad:

- la interfaz usa almacenamiento en memoria;
- el Project externo se materializo durante la auditoria final, no desde el
  primer dia;
- no existe un commit significativo del 27 de julio y no se alteraron fechas
  para fabricarlo;
- la revision cruzada de la pareja debe demostrarse verbalmente y mediante los
  commits reales disponibles.

Respuesta recomendada:

> Identificamos esas limitaciones durante la auditoria. Corregimos todo lo que
> podia corregirse tecnicamente, documentamos lo que no podia reconstruirse
> honestamente y no alteramos el historial.

## 15. Cierre sugerido

> El incremento termina con una aplicacion ejecutable por interfaz y consola,
> 99 pruebas automatizadas, reglas de dominio separadas, evidencia RED-GREEN-
> REFACTOR, tablero Kanban, CI con Java y PostgreSQL, y uso transparente de IA.
> La principal mejora futura seria conectar la interfaz al repositorio
> PostgreSQL manteniendo el contrato y las pruebas actuales.

## 16. Lista final de comprobacion

- [ ] Java 17 o superior seleccionado.
- [ ] `mvn clean verify` en verde.
- [ ] JAR generado.
- [ ] Interfaz abre correctamente.
- [ ] Datos de ejemplo cargan.
- [ ] Se demuestra prioridad CRITICA.
- [ ] Se demuestra EXPEDITE.
- [ ] Se demuestra una transicion.
- [ ] Se finaliza con solucion.
- [ ] Se muestran filtros y metricas.
- [ ] GitHub Actions abierto y verde.
- [ ] Tablero Kanban disponible.
- [ ] `IA-LOG.md`, `REFACTOR.md` y retrospectiva listos.
- [ ] Ambos integrantes pueden explicar cualquier componente.
