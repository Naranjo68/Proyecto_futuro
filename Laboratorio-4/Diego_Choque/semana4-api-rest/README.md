# Guía de Laboratorio N.° 04 - API REST con Spring Boot
**Curso:** Desarrollo Web Integrado (100000ST61)  
**Semana:** 4  
**Estudiante:** Diego Choque  
**Proyecto:** `semana4-api-rest`  

---

## 1. Descripción del Proyecto
Implementación completa de una API RESTful para la gestión del catálogo de productos usando Spring Boot, métodos HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`), DTOs con validaciones de `jakarta.validation`, manejo global de errores centralizado con `@RestControllerAdvice` y almacenamiento temporal seguro para concurrencia en memoria con `ConcurrentHashMap` y `AtomicLong`.

---

## 2. Documentación de Endpoints (Ejercicio 3)

| Método | Endpoint / URI | Parámetros / Body Esperado | Código HTTP | Descripción |
|---|---|---|---|---|
| **GET** | `/api/productos` | Query param opcional: `categoria` (ej. `?categoria=Tecnologia`) | `200 OK` | Obtiene la lista de todos los productos o filtrados por categoría. |
| **GET** | `/api/productos/buscar` | Query param: `texto` (ej. `?texto=Lenovo`) | `200 OK` | **Ejercicio 1:** Busca productos cuyo nombre contenga el texto indicado (insensible a mayúsculas). |
| **GET** | `/api/productos/{id}` | Path variable: `id` (ej. `/api/productos/1`) | `200 OK` / `404 Not Found` | Obtiene el detalle de un producto por su ID. |
| **POST** | `/api/productos` | JSON Body (`ProductoRequest`):<br>`{"nombre": "Monitor", "categoria": "Tecnologia", "precio": 920.50, "stock": 8}` | `201 Created` / `400 Bad Request` | Crea un nuevo producto. Incluye header `Location` con la URI del recurso creado. |
| **PUT** | `/api/productos/{id}` | Path variable: `id`<br>JSON Body (`ProductoRequest`):<br>`{"nombre": "Laptop ThinkPad", "categoria": "Tecnologia", "precio": 3899.90, "stock": 12}` | `200 OK` / `400 Bad Request` / `404 Not Found` | Reemplaza o actualiza completamente un producto existente. |
| **PATCH** | `/api/productos/{id}/stock` | Path variable: `id`<br>JSON Body (`ActualizarStockRequest`):<br>`{"stock": 20}` | `200 OK` / `400 Bad Request` / `404 Not Found` | Actualiza únicamente el stock de un producto existente. |
| **PATCH** | `/api/productos/{id}/disminuir-stock` | Path variable: `id`<br>JSON Body (`DisminuirStockRequest`):<br>`{"cantidad": 3}` | `200 OK` / `400 Bad Request` / `404 Not Found` | **Ejercicio 2:** Disminuye el stock de un producto. Si la cantidad supera el stock actual, devuelve error 400. |
| **DELETE** | `/api/productos/{id}` | Path variable: `id` | `204 No Content` / `404 Not Found` | Elimina un producto por su identificador. |

---

## 3. Ejercicios Complementarios Implementados

### Ejercicio 1: Búsqueda por texto
- **Endpoint:** `GET /api/productos/buscar?texto=lap`
- **Comportamiento:** Realiza un filtrado sobre la colección en memoria evaluando que `nombre.toLowerCase().contains(texto.toLowerCase())`.
- **Respuesta:** Lista de productos que coinciden con el criterio (ej. "Laptop Lenovo").

### Ejercicio 2: Disminuir stock
- **Endpoint:** `PATCH /api/productos/{id}/disminuir-stock`
- **Body:** `{"cantidad": 5}`
- **Regla de negocio:** No se permite que el stock quede negativo. Si la cantidad a disminuir es superior al stock actual, se lanza `StockInsuficienteException` y el manejador global responde con código `400 Bad Request` y mensaje descriptivo.

---

## 4. Respuestas de Error Uniformes

Todos los errores devuelven un JSON estructurado modelado por `ErrorResponse`:
```json
{
  "estado": 400,
  "mensaje": "El nombre es obligatorio; El precio debe ser mayor a cero",
  "ruta": "/api/productos",
  "fechaHora": "2026-09-03T22:05:00.123456"
}
```
- `404 Not Found`: Cuando el producto con el ID solicitado no existe.
- `400 Bad Request`: Cuando fallan las validaciones de bean validation (`@NotBlank`, `@Positive`, `@Min`, `@NotNull`) o cuando se violan reglas de negocio (ej. disminuir stock por encima de lo disponible).

---

## 5. Pruebas Rápidas con cURL

```bash
# 1. Listar productos
curl -X GET http://localhost:8080/api/productos

# 2. Filtrar por categoría
curl -X GET "http://localhost:8080/api/productos?categoria=Tecnologia"

# 3. Buscar por texto parcial (Ejercicio 1)
curl -X GET "http://localhost:8080/api/productos/buscar?texto=Lenovo"

# 4. Obtener por ID
curl -X GET http://localhost:8080/api/productos/1

# 5. Crear un producto
curl -i -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Monitor Samsung","categoria":"Tecnologia","precio":920.50,"stock":8}'

# 6. Actualizar completamente (PUT)
curl -X PUT http://localhost:8080/api/productos/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Laptop Lenovo ThinkPad","categoria":"Tecnologia","precio":3899.90,"stock":12}'

# 7. Actualizar parcialmente stock (PATCH)
curl -X PATCH http://localhost:8080/api/productos/1/stock \
  -H "Content-Type: application/json" \
  -d '{"stock":25}'

# 8. Disminuir stock (PATCH - Ejercicio 2)
curl -X PATCH http://localhost:8080/api/productos/1/disminuir-stock \
  -H "Content-Type: application/json" \
  -d '{"cantidad":5}'

# 9. Eliminar producto (DELETE)
curl -i -X DELETE http://localhost:8080/api/productos/1

# 10. Probar 404
curl -i -X GET http://localhost:8080/api/productos/999

# 11. Probar 400 por validación
curl -i -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"","categoria":"Tecnologia","precio":-50,"stock":-1}'
```

---

## 6. Respuestas a las Preguntas de Reflexión (Sección 29)

1. **¿Por qué GET no debe usarse para crear, actualizar o eliminar datos?**  
   Porque la especificación HTTP define a `GET` como un método seguro e idempotente que solo debe recuperar información sin producir efectos secundarios ni mutar el estado del servidor. Además, las solicitudes GET pueden ser cacheadas por navegadores, proxies y CDNs, o pre-cargadas por motores de búsqueda, lo que podría provocar modificaciones o eliminaciones accidentales de datos.

2. **¿Qué diferencia existe entre POST y PUT?**  
   `POST` se utiliza para crear un nuevo recurso subordinado a una colección, donde el servidor suele asignar el identificador y no es idempotente. `PUT` se utiliza para reemplazar completamente un recurso existente o crearlo en una URI específica conocida; es idempotente porque ejecutar la misma petición múltiples veces deja al recurso en el mismo estado final.

3. **¿Qué diferencia existe entre PUT y PATCH?**  
   `PUT` reemplaza la representación completa del recurso (requiere enviar todos los atributos). `PATCH` realiza una actualización parcial, aplicando modificaciones únicamente a los campos especificados en la solicitud (por ejemplo, actualizar únicamente el stock).

4. **¿Por qué es útil `ResponseEntity` en una API REST?**  
   `ResponseEntity` representa la respuesta HTTP completa, permitiendo al desarrollador controlar de forma explícita y programática el código de estado HTTP (ej. 200, 201, 204, 400, 404), las cabeceras HTTP (como `Location`, `Content-Type`) y el cuerpo de la respuesta serializado a JSON.

5. **¿Qué función cumple `@RequestBody`?**  
   Deserializa automáticamente el cuerpo de la petición HTTP (usualmente JSON recibido en el request) y lo convierte en una instancia del objeto Java correspondiente (DTO o modelo) usando el `HttpMessageConverter` configurado en Spring.

6. **¿Qué diferencia hay entre `@PathVariable` y `@RequestParam`?**  
   `@PathVariable` extrae valores embebidos directamente en la ruta de la URI que identifican un recurso específico (ej. `/api/productos/{id}`). `@RequestParam` extrae parámetros de consulta (query parameters) enviados tras el signo de interrogación (ej. `/api/productos?categoria=Tecnologia`), comúnmente utilizados para filtrar, buscar o paginar.

7. **¿Por qué conviene usar DTOs en lugar de recibir directamente cualquier objeto?**  
   Los DTOs (*Data Transfer Objects*) desacoplan la API pública del modelo de datos interno de la aplicación. Permiten aplicar reglas de validación específicas de entrada, evitan problemas de seguridad como *Mass Assignment* (donde un cliente malintencionado podría sobreescribir campos sensibles como IDs o roles), y evitan exponer atributos privados o dependencias de persistencia.

8. **¿Qué significa devolver un error 400?**  
   Indica `Bad Request`: el servidor no puede o no procesará la solicitud debido a un error del cliente (sintaxis inválida, parámetros incorrectos, fallos en las validaciones de campos requeridos o violación de reglas de negocio).

9. **¿Qué significa devolver un error 404?**  
   Indica `Not Found`: el servidor no ha encontrado ninguna coincidencia para la URI solicitada, comúnmente porque el recurso con el identificador indicado no existe en el sistema.

10. **¿Qué ventaja tiene centralizar errores con `@RestControllerAdvice`?**  
    Permite interceptar y procesar excepciones lanzadas por cualquier controlador en un único lugar de manera desacoplada. Elimina la duplicación de bloques `try-catch` en cada método, estandariza el formato y estructura de las respuestas de error hacia el cliente y facilita el mantenimiento y evolución del manejo de incidencias.

---

## 7. Checklist de Verificación Funcional (Sección 26)

| N.° | Verificación | Cumple |
|---|---|:---:|
| 1 | El proyecto compila sin errores. | **Sí** |
| 2 | La aplicación inicia en el puerto 8080. | **Sí** |
| 3 | `GET /api/productos` devuelve una lista JSON. | **Sí** |
| 4 | `GET /api/productos/{id}` devuelve un producto existente. | **Sí** |
| 5 | `POST /api/productos` crea un producto y devuelve 201. | **Sí** |
| 6 | `PUT /api/productos/{id}` actualiza un producto completo. | **Sí** |
| 7 | `PATCH /api/productos/{id}/stock` actualiza solo el stock. | **Sí** |
| 8 | `DELETE /api/productos/{id}` devuelve 204. | **Sí** |
| 9 | Los datos inválidos devuelven 400. | **Sí** |
| 10 | Un producto inexistente devuelve 404. | **Sí** |
| 11 | `mvn test` ejecuta las pruebas correctamente. | **Sí** (13 tests pasaron con éxito) |
| 12 | `mvn clean package` genera el JAR. | **Sí** (`target/semana4-api-rest-0.0.1-SNAPSHOT.jar`) |
