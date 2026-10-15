# Preguntas sobre JPA, Hibernate y Spring Data JPA

## 1. ¿Cuál es la diferencia entre JPA e Hibernate?

**JPA (Java Persistence API)** es una especificación de Java que define cómo una aplicación puede realizar persistencia de objetos en una base de datos relacional mediante ORM (Object-Relational Mapping). JPA establece interfaces, anotaciones y reglas, pero no realiza por sí misma la implementación.

**Hibernate** es una implementación de JPA. Es decir, proporciona el código necesario para ejecutar las operaciones de persistencia definidas por la especificación JPA.

En resumen:

- **JPA:** especificación o estándar.
- **Hibernate:** implementación de esa especificación.
- Spring Data JPA permite trabajar con JPA de una forma más sencilla, normalmente utilizando Hibernate como proveedor de persistencia.

---

## 2. ¿Qué ventaja ofrece Spring Data JPA frente a implementar acceso JDBC manual?

La principal ventaja es que **reduce considerablemente la cantidad de código necesario para acceder a la base de datos**.

Con JDBC manual normalmente es necesario escribir código para:

- Abrir y cerrar conexiones.
- Preparar consultas SQL.
- Ejecutar `PreparedStatement`.
- Recorrer `ResultSet`.
- Convertir manualmente los resultados de la base de datos a objetos Java.
- Manejar excepciones relacionadas con JDBC.

Con Spring Data JPA podemos definir una interfaz como `ProductoRepository` y heredar de `JpaRepository`. Spring genera automáticamente gran parte de la implementación necesaria para operaciones como guardar, buscar, actualizar y eliminar registros.

Por ejemplo:

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
```

Esto permite utilizar métodos como `save()`, `findAll()`, `findById()` y `deleteById()` sin implementar manualmente las consultas básicas.

---

## 3. ¿Por qué ProductoRepository es una interfaz y aun así puede inyectarse como objeto?

Porque **Spring Data JPA crea automáticamente una implementación de la interfaz en tiempo de ejecución**.

Cuando declaramos:

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
```

no necesitamos crear manualmente una clase como `ProductoRepositoryImpl`.

Spring detecta la interfaz mediante el mecanismo de repositorios de Spring Data y genera un objeto que implementa esa interfaz. Luego, mediante **inyección de dependencias**, ese objeto puede ser utilizado en un Controller o Service.

Por ejemplo:

```java
@Autowired
private ProductoRepository productoRepository;
```

Por lo tanto, aunque `ProductoRepository` sea una interfaz, Spring proporciona automáticamente una implementación concreta que puede ser inyectada.

---

## 4. ¿Qué función cumple `@Entity`?

La anotación `@Entity` indica que una clase Java representa una **entidad persistente** que puede ser almacenada en una tabla de una base de datos mediante JPA.

Por ejemplo:

```java
@Entity
public class Producto {
    // atributos y métodos
}
```

Al utilizar `@Entity`, JPA considera que los objetos de esa clase pueden ser gestionados por el contexto de persistencia.

Generalmente, una entidad también necesita un identificador marcado con `@Id`:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

JPA utiliza esta información para relacionar los objetos Java con los registros de la base de datos.

---

## 5. ¿Qué ocurre con el identificador cuando se utiliza `GenerationType.IDENTITY`?

Cuando se utiliza:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

el **identificador es generado automáticamente por la base de datos** al insertar un nuevo registro.

Por ejemplo, si la tabla tiene una columna `id` autoincremental, la aplicación no necesita asignar manualmente el valor del identificador.

Un ejemplo sería:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Si se insertan tres productos, la base de datos podría generar automáticamente los identificadores:

```text
Producto 1 → id = 1
Producto 2 → id = 2
Producto 3 → id = 3
```

El valor exacto depende del mecanismo de generación configurado en la base de datos.

---

## 6. ¿Por qué el Controller de la semana 5 necesitó pocos cambios al introducir la base de datos?

Porque la aplicación ya estaba organizada siguiendo una **separación de responsabilidades**, donde el Controller se encarga principalmente de recibir las solicitudes y coordinar las respuestas, mientras que el acceso a los datos se realiza mediante el Service y el Repository.

Al introducir JPA y Spring Data JPA, gran parte de la lógica relacionada con la persistencia se trasladó al Repository.

Por ejemplo, el Controller puede seguir realizando una operación sencilla:

```java
productoService.listarProductos();
```

El cambio principal ocurre en las capas internas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
Base de datos
```

Por esta razón, si la interfaz del Service se mantiene estable, el Controller no necesita conocer los detalles de si los datos provienen de una lista en memoria, JDBC o una base de datos mediante JPA.

---

## 7. ¿Qué riesgo existe al usar `ddl-auto=update` en producción?

El principal riesgo es que **Hibernate puede modificar automáticamente la estructura de la base de datos al iniciar la aplicación**, basándose en las entidades y sus cambios.

Por ejemplo, si se modifica una entidad, Hibernate puede intentar alterar las tablas correspondientes. En un entorno de producción esto puede provocar:

- Cambios inesperados en el esquema.
- Problemas de compatibilidad con datos existentes.
- Pérdida o alteración de información en determinados cambios de estructura.
- Dificultad para controlar y auditar las modificaciones de la base de datos.
- Diferencias entre los esquemas de distintos entornos.

Por ello, `ddl-auto=update` puede ser útil durante el desarrollo, pero en producción normalmente se recomienda **controlar las migraciones mediante herramientas específicas**, como Flyway o Liquibase, y evitar que Hibernate modifique libremente el esquema.

---

## Conclusión

JPA proporciona el estándar para la persistencia de objetos Java, mientras que Hibernate es una de sus implementaciones más utilizadas. Spring Data JPA simplifica todavía más este trabajo al generar automáticamente implementaciones de repositorios y proporcionar métodos CRUD.

Gracias a esta arquitectura, la aplicación puede mantener una separación clara entre **Controller, Service, Repository y base de datos**, haciendo que los cambios relacionados con la persistencia tengan un impacto reducido en las demás capas.
