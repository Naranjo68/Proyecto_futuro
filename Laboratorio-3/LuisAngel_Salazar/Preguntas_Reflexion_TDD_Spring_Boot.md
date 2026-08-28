# Preguntas de Reflexión — Laboratorio N.° 03: TDD en Spring Boot

## 1. ¿Por qué TDD no es lo mismo que escribir pruebas después de terminar el código?

Porque en TDD las pruebas se escriben **antes de implementar la funcionalidad**. La prueba define el comportamiento esperado y luego se desarrolla el código mínimo necesario para hacerla pasar.

## 2. ¿Qué representa la fase RED?

Representa una prueba que **falla inicialmente**, porque la funcionalidad todavía no existe o no cumple la regla esperada.

## 3. ¿Por qué no debe saltarse la fase REFACTOR?

Porque permite mejorar la estructura, los nombres y la organización del código manteniendo el comportamiento comprobado por las pruebas.

## 4. ¿Qué diferencia hay entre probar `ProductoService` y `ProductoController`?

`ProductoServiceTest` prueba directamente la **lógica de negocio**.

`ProductoControllerTest` prueba la **capa HTTP**, verificando códigos de respuesta y JSON mediante MockMvc.

## 5. ¿Para qué sirve MockMvc?

Sirve para probar controladores Spring MVC/REST sin tener que levantar un servidor HTTP real.

## 6. ¿Por qué en `@WebMvcTest` se usa un mock del servicio?

Porque queremos probar únicamente el controlador. El `ProductoService` se reemplaza por un mock para aislar la capa web y controlar las respuestas que entrega el servicio.

## 7. ¿Qué error se genera cuando Angular o Postman envía un JSON con tipos incorrectos?

Normalmente se produce:

`400 Bad Request`

Este error corresponde a un JSON mal formado o a tipos de datos incorrectos.

## 8. ¿Cómo ayuda TDD a mejorar el diseño de una API REST?

TDD obliga a definir primero el comportamiento esperado, ayuda a separar responsabilidades y permite detectar rápidamente si un cambio rompe una funcionalidad existente.
