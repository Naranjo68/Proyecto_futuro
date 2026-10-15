package com.kala.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Deshabilitado hasta A2-L5: arrancar el contexto completo exige base de datos y secretos, y los
// módulos del segundo avance se integran recién en esa fase.
@Disabled("Se reactiva en A2-L5 (integración)")
@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
