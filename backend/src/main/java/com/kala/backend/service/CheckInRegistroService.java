package com.kala.backend.service;

import com.kala.backend.dto.CheckInEnvioRequest;
import com.kala.backend.exception.CheckInDuplicadoException;
import com.kala.backend.exception.ConfiguracionLdpNoVigenteException;
import com.kala.backend.exception.ItemCuestionarioNoEncontradoException;
import com.kala.backend.repository.AgregadoItemRepository;
import com.kala.backend.repository.SesionCheckinRepository;
import com.kala.backend.service.ItemCuestionarioLector.ItemCuestionarioInfo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Registra un check-in como una sola transacción: o se guardan la sesión y la suma, o no se guarda nada.
// El servidor recibe la respuesta ya perturbada en el dispositivo y solo la suma; no guarda quién respondió qué.
@Service
public class CheckInRegistroService {

    private final ItemCuestionarioLector items;
    private final SesionCheckinRepository sesiones;
    private final AgregadoItemRepository agregados;
    private final String secreto;
    private final ZoneId zona;
    private final Clock reloj;

    @Autowired
    public CheckInRegistroService(ItemCuestionarioLector items,
                                  SesionCheckinRepository sesiones,
                                  AgregadoItemRepository agregados,
                                  @Value("${kala.sesion.secreto}") String secreto,
                                  @Value("${kala.ventana.zona}") String zona) {
        this(items, sesiones, agregados, secreto, zona, Clock.systemUTC());
    }

    // Constructor para pruebas, con un reloj controlable.
    CheckInRegistroService(ItemCuestionarioLector items,
                           SesionCheckinRepository sesiones,
                           AgregadoItemRepository agregados,
                           String secreto,
                           String zona,
                           Clock reloj) {
        this.items = items;
        this.sesiones = sesiones;
        this.agregados = agregados;
        this.secreto = secreto;
        this.zona = ZoneId.of(zona);
        this.reloj = reloj;
    }

    @Transactional
    public void registrar(Long usuarioId, Long equipoId, CheckInEnvioRequest request) {
        ItemCuestionarioInfo item = items.buscar(request.itemId())
                .orElseThrow(() -> new ItemCuestionarioNoEncontradoException(request.itemId()));

        if (!item.configuracionLdpId().equals(request.configuracionLdpId())) {
            throw new ConfiguracionLdpNoVigenteException(request.itemId());
        }

        Instant ahora = reloj.instant();
        LocalDate ventana = VentanaSemanal.inicioDeVentana(ahora, zona);
        String hash = SesionHasher.hashDeSesion(secreto, usuarioId, item.cuestionarioId(), ventana);

        // 0 filas = el hash ya existía: esta persona ya respondió en esta ventana.
        if (sesiones.insertarSiNoExiste(hash, item.cuestionarioId(), ventana) == 0) {
            throw new CheckInDuplicadoException();
        }

        // Solo fecha, nunca hora (R5): el agregado no permite correlacionar por tiempo.
        LocalDate hoy = ahora.atZone(zona).toLocalDate();
        BigDecimal suma = Boolean.TRUE.equals(request.respuestaRuidosa()) ? BigDecimal.ONE : BigDecimal.ZERO;
        agregados.registrarRespuesta(equipoId, request.itemId(), request.configuracionLdpId(), hoy, suma);
    }
}
