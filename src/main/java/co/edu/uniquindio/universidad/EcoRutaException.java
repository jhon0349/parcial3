package co.edu.uniquindio.universidad;

/** Excepción de dominio: se lanza cuando una operación es rechazada. */
public class EcoRutaException extends RuntimeException {
    public EcoRutaException(String mensaje) {
        super(mensaje);
    }
}
