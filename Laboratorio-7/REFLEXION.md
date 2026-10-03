# Reflexión: JPA, Spring Data JPA y Transacciones

## 1. ¿Por qué JPQL utiliza el nombre de la entidad y no el nombre de la tabla?

JPQL (Java Persistence Query Language) trabaja con el **modelo de objetos de Java**, no directamente con las tablas de la base de datos. Por eso, las consultas JPQL utilizan el nombre de la entidad y sus atributos.

Por ejemplo, si tenemos una entidad `Producto` que está mapeada a una tabla llamada `productos`, en JPQL se escribiría:

```java
SELECT p FROM Producto p WHERE p.precio > :precio
```

La ventaja es que la consulta se mantiene relacionada con el modelo de dominio y JPA/Hibernate se encarga de traducirla al SQL correspondiente según el mapeo definido.

---

## 2. ¿Cuándo elegirías una consulta derivada y cuándo `@Query`?

Elegiría una **consulta derivada** cuando la operación es sencilla y puede expresarse claramente mediante el nombre del método.

Ejemplo:

```java
List<Producto> findByCategoria(String categoria);
```

En cambio, utilizaría `@Query` cuando la consulta sea más compleja, necesite condiciones específicas, `JOIN`, agregaciones, una consulta personalizada o cuando el nombre del método derivado sería demasiado largo y difícil de mantener.

Ejemplo:

```java
@Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :min AND :max")
List<Producto> buscarPorRangoPrecio(double min, double max);
```

En conclusión, para consultas simples prefiero consultas derivadas por su sencillez; para consultas complejas, `@Query` ofrece mayor control.

---

## 3. ¿Por qué la frontera transaccional se ubica normalmente en la capa Service?

La capa **Service** representa la lógica de negocio y es el lugar donde normalmente se agrupan varias operaciones que deben ejecutarse como una sola unidad.

Por ejemplo, confirmar un pedido puede implicar:

1. Crear el pedido.
2. Registrar sus detalles.
3. Descontar el stock.
4. Registrar el movimiento de inventario.
5. Limpiar el carrito.

Todas estas operaciones deberían completarse correctamente o revertirse juntas. Por eso, una anotación como `@Transactional` suele colocarse en el método de la capa Service.

Esto también permite mantener una separación clara de responsabilidades: el Repository se ocupa del acceso a datos, mientras que el Service coordina las operaciones de negocio y define la frontera transaccional.

---

## 4. ¿Qué diferencia existe entre llamar `save()` y confiar en dirty checking dentro de una transacción?

`save()` indica explícitamente a Spring Data JPA que queremos persistir o actualizar una entidad. Dependiendo del estado de la entidad, puede terminar realizando una operación de `persist` o de actualización.

El **dirty checking**, en cambio, permite modificar una entidad que ya está gestionada por el `EntityManager` dentro de una transacción sin necesidad de llamar explícitamente a `save()`.

Por ejemplo:

```java
@Transactional
public void actualizarPrecio(Long id) {
    Producto producto = repository.findById(id).orElseThrow();
    producto.setPrecio(50.0);
}
```

Al finalizar la transacción, Hibernate detecta que `producto` cambió y genera el `UPDATE` correspondiente.

Por tanto, `save()` es una operación explícita de persistencia, mientras que el dirty checking permite que JPA detecte automáticamente cambios realizados sobre entidades gestionadas.

---

## 5. ¿Qué ocurriría si el stock se descuenta pero el registro del movimiento falla y no existiera una transacción?

Podría producirse una **inconsistencia de datos**.

Por ejemplo:

- El stock pasa de `10` a `9`.
- Luego falla el registro del movimiento.
- Como no existe una transacción que agrupe ambas operaciones, el descuento del stock podría permanecer guardado.

La base de datos terminaría indicando que se descontó una unidad, pero no existiría el registro que explica ese movimiento.

Con una transacción, si el registro del movimiento falla, se puede realizar un **rollback**, haciendo que también se revierta el descuento del stock.

---

## 6. ¿Qué tipo de excepción provoca rollback por defecto en Spring?

En Spring, una transacción anotada con `@Transactional` realiza **rollback por defecto ante excepciones no comprobadas (`RuntimeException`) y errores (`Error`)**.

Las excepciones comprobadas (`checked exceptions`) no provocan rollback automáticamente por defecto.

Si se necesita hacer rollback ante una excepción comprobada, se puede configurar explícitamente:

```java
@Transactional(rollbackFor = Exception.class)
```

Esto permite adaptar el comportamiento transaccional a las necesidades de la lógica de negocio.

---

## 7. ¿Por qué una transacción no debería mantenerse abierta mientras se realiza una llamada remota lenta?

Porque una transacción abierta mantiene recursos de la base de datos ocupados durante toda su ejecución.

Si dentro de la transacción se realiza una llamada HTTP, consulta a otro servicio o cualquier operación remota lenta, pueden ocurrir problemas como:

- Conexiones de base de datos retenidas durante más tiempo.
- Bloqueos mantenidos innecesariamente.
- Mayor consumo del pool de conexiones.
- Mayor posibilidad de timeouts.
- Menor capacidad para atender otras solicitudes concurrentes.

Por eso, normalmente se intenta mantener la frontera transaccional lo más corta posible y evitar llamadas remotas lentas dentro de ella.

---

## 8. ¿Qué problema podría aparecer si devolvemos directamente entidades JPA con relaciones `LAZY` como respuesta REST?

Puede aparecer el problema conocido como **`LazyInitializationException`**.

Una relación `LAZY` no se carga inmediatamente; Hibernate intenta cargarla cuando realmente se necesita. Si la entidad se devuelve después de que la sesión o el contexto de persistencia ya se cerró, intentar acceder a esa relación puede provocar una excepción.

Además, devolver directamente entidades JPA puede generar otros problemas:

- Exponer detalles internos del modelo de persistencia.
- Serializaciones inesperadas.
- Recursión infinita cuando existen relaciones bidireccionales.
- Consultas adicionales no deseadas.
- Problemas de rendimiento por el patrón **N+1**.

Por estas razones, en APIs REST suele ser preferible utilizar **DTOs**, definiendo exactamente qué información debe exponerse en la respuesta.

---

## Conclusión

Estos conceptos muestran que JPA y Spring Data JPA no solo simplifican el acceso a la base de datos, sino que también requieren comprender cómo se relacionan el modelo de objetos, el contexto de persistencia y las transacciones.

La elección adecuada entre consultas derivadas y `@Query`, el uso correcto de `@Transactional`, el dirty checking y el manejo de relaciones `LAZY` permiten construir aplicaciones más consistentes, mantenibles y eficientes.

En especial, la capa Service cumple un papel fundamental al coordinar la lógica de negocio y establecer límites transaccionales que aseguren que las operaciones relacionadas se ejecuten de forma consistente.
