# Sistema de Incidencias Xanpan

[![Integracion continua](https://github.com/Robert-Granados/SISTEMA-DE-INCIDENCIAS-XANPAN/actions/workflows/ci.yml/badge.svg?branch=feature%2FAlejandro)](https://github.com/Robert-Granados/SISTEMA-DE-INCIDENCIAS-XANPAN/actions/workflows/ci.yml)

Aplicacion academica para registrar incidencias de soporte, calcular su prioridad, controlar el flujo Xanpan, aplicar la politica EXPEDITE y consultar metricas operativas.

## Integrantes

- Robert Granados Perez
- Alejandro Bolanos Chinchilla

Proyecto configurado con:

- Java 17 o superior
- Maven
- JUnit 5

## Requisitos

- JDK 17 o una versión posterior
- Maven 3.9 o una versión compatible

Comprueba las instalaciones:

```bash
java -version
mvn -version
```

## Ejecutar las pruebas

Desde la raíz del proyecto:

```bash
mvn test
```

Para compilar, ejecutar todas las verificaciones y generar el artefacto:

```bash
mvn clean verify
```

El código de producción está en `src/main/java` y las pruebas en
`src/test/java`.

## Ejecutar la aplicacion

Construye el JAR y abre la interfaz grafica desde la raiz:

```bash
mvn clean package
java -jar target/sistema-incidencias-1.0.0-SNAPSHOT.jar
```

La interfaz permite registrar, buscar y filtrar incidencias; avanzar estados;
marcar EXPEDITE; finalizar con una solucion y consultar metricas.

La demostracion original de consola sigue disponible:

```bash
java -jar target/sistema-incidencias-1.0.0-SNAPSHOT.jar --console
```

Los datos de la interfaz se guardan en memoria durante la ejecucion. El boton
`Cargar datos de ejemplo` prepara un escenario rapido para la defensa.

## Base de datos PostgreSQL

La base de datos se ejecuta en Docker y se inicializa automáticamente con el
esquema, las reglas de negocio y las categorías predeterminadas:

```bash
docker compose up -d --wait
```

La conexión predeterminada para desarrollo es:

- Servidor: `localhost`
- Puerto: `5432`
- Base de datos: `xanpan`
- Usuario: `xanpan`
- Contraseña: `xanpan_dev`

Para personalizar estos valores, copia `.env.example` como `.env` y cambia las
credenciales. No utilices la contraseña predeterminada en producción.

Ejecutar las pruebas del esquema:

```bash
docker cp database/tests/schema_tests.sql xanpan-postgres:/tmp/schema_tests.sql
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U xanpan -d xanpan -f /tmp/schema_tests.sql
```

Detener la base de datos conservando sus datos:

```bash
docker compose down
```

Eliminar también el volumen de datos y reinicializarla en el próximo arranque:

```bash
docker compose down --volumes
docker compose up -d --wait
```

Los scripts de `database/init` solamente se ejecutan al crear un volumen vacío.

## Calidad y documentacion

La suite contiene pruebas unitarias y dos flujos funcionales etiquetados con
`functional`. Para ejecutar solamente esos flujos:

```bash
mvn -Dgroups=functional test
```

El pipeline de integracion continua compila, ejecuta las pruebas Java y valida
el esquema PostgreSQL en cada `push` y `pull_request`.

- [Tablero Kanban y limites WIP](docs/KANBAN.md)
- [Evidencia de TDD y trazabilidad](docs/EVIDENCIAS-TDD.md)
- [Guia para explicar la interfaz](docs/INTERFAZ.md)
- [Bitacora de uso de IA](IA-LOG.md)
- [Retrospectiva](RETROSPECTIVA.md)

## Tablero y decisiones de diseno

- [GitHub Project: HelpDesk Flow - Xanpan](https://github.com/users/AlejandroXV5/projects/1)
- [Tablero versionado y politicas WIP](docs/KANBAN.md)

Decisiones principales:

- El dominio no depende de infraestructura; `IncidentRepository` permite usar memoria en pruebas y PostgreSQL como persistencia.
- `StateTransitionValidator` mantiene pura la maquina de estados y `ExpeditePolicy` concentra la regla de capacidad activa.
- `closedAt` representa el cierre real; un `Clock` inyectable hace deterministas las pruebas de throughput y lead time.
- Las reglas se protegen en Java y las restricciones de integridad se verifican tambien en PostgreSQL.

## Estado de integracion continua

El workflow `ci.yml` se ejecuta en cada `push` y `pull_request`, compila con Java 17, ejecuta las 99 pruebas Java y valida el esquema PostgreSQL. El distintivo superior apunta a la rama `feature/Alejandro`.
