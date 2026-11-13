package co.edu.uniquindio.universidad;

/** Aportante registrado. Sus datos no se modifican después de creados (record inmutable). */
public record Aportante(String identificacion, String nombre, String correo) {

    public Aportante {
        if (identificacion == null || identificacion.isBlank()) {
            throw new EcoRutaException("La identificación del aportante es obligatoria.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new EcoRutaException("El nombre del aportante es obligatorio.");
        }
        if (correo == null || correo.isBlank()) {
            throw new EcoRutaException("El correo del aportante es obligatorio.");
        }
    }
}
