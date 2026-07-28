# Guia para explicar la interfaz

## Que se construyo

La aplicacion incluye una interfaz de escritorio Java Swing. Se eligio Swing
porque forma parte del JDK, no requiere un servidor web y permite conservar el
JAR ejecutable sin dependencias graficas adicionales.

La ventana contiene:

- un formulario para registrar incidencias;
- una tabla con busqueda y filtros;
- acciones para avanzar el estado, marcar EXPEDITE y finalizar;
- un panel con todos los datos de la incidencia seleccionada;
- metricas de total, abiertas, cerradas, throughput y lead time;
- un boton para cargar un escenario de demostracion.

## Separacion de responsabilidades

```text
App
 |
 +-- IncidentFrame       Presenta datos y captura acciones
       |
       +-- IncidentController   Coordina el caso de uso
             |
             +-- IncidentService       Registro y transiciones
             +-- ExpediteService       Regla EXPEDITE
             +-- MetricsService        Calculos operativos
             +-- IncidentRepository    Acceso a incidencias
```

`IncidentFrame` no calcula prioridades ni decide si una transicion es valida.
Cuando el usuario presiona un boton, la ventana llama a `IncidentController`.
El controlador traduce la accion a los servicios existentes y estos aplican
las reglas del dominio. Si una regla falla, la interfaz muestra el mensaje sin
alterar la incidencia.

El controlador no importa ninguna clase Swing. Por eso sus operaciones se
prueban sin abrir ventanas en `IncidentControllerTest`.

## Recorrido recomendado para la demostracion

1. Ejecutar `mvn clean verify` y mostrar las 99 pruebas verdes.
2. Ejecutar el JAR sin argumentos para abrir la ventana.
3. Presionar `Cargar datos de ejemplo`.
4. Seleccionar la incidencia critica y explicar la prioridad automatica y
   la marca EXPEDITE.
5. Avanzar una incidencia por REGISTRADA, LISTA, EN DESARROLLO y EN VALIDACION.
6. Presionar `Finalizar con solucion` y escribir la solucion aplicada.
7. Mostrar que cambian abiertas, cerradas, throughput y lead time.
8. Usar los filtros para mostrar solamente criticas o finalizadas.

## Respuestas tecnicas breves

### Por que la interfaz no accede directamente al repositorio

Porque eso duplicaria reglas y acoplaria la ventana al almacenamiento. La
interfaz usa un controlador y los servicios del dominio.

### Donde se calcula la prioridad

En `PriorityCalculator`, llamado por `IncidentService` al registrar. La ventana
solamente envia impacto y urgencia.

### Donde se valida EXPEDITE

En `ExpeditePolicy`, utilizada por `ExpediteService` al marcar y por
`IncidentService` al entrar en un estado activo.

### Por que finalizar tiene un boton separado

El paso desde EN VALIDACION hasta FINALIZADA exige una descripcion de solucion.
El boton solicita ese dato y llama a `completeIncident`.

### Los datos sobreviven al cerrar la ventana

No. La interfaz usa `InMemoryIncidentRepository`, adecuado para la demostracion
y las pruebas. El esquema PostgreSQL se valida por separado y puede conectarse
en un incremento posterior mediante la interfaz `IncidentRepository`.

## Comandos

```powershell
# Seleccionar Java 17 o superior en la terminal actual
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Compilar y probar
mvn clean verify

# Interfaz grafica
java -jar target\sistema-incidencias-1.0.0-SNAPSHOT.jar

# Demostracion de consola
java -jar target\sistema-incidencias-1.0.0-SNAPSHOT.jar --console
```
