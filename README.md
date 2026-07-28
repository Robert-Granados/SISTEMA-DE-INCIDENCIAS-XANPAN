# Sistema de Incidencias Xanpan

[![Integracion continua](https://github.com/Robert-Granados/SISTEMA-DE-INCIDENCIAS-XANPAN/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Robert-Granados/SISTEMA-DE-INCIDENCIAS-XANPAN/actions/workflows/ci.yml)

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
mvn verify
```

El código de producción está en `src/main/java` y las pruebas en
`src/test/java`.

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
- [Bitacora de uso de IA](IA-LOG.md)
- [Retrospectiva](RETROSPECTIVA.md)
