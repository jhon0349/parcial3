package co.edu.uniquindio.universidad;

public abstract class Lote {

    private final String codigo;
    private final String descripcion;
    private final int pesoKg;

    protected Lote(String codigo, String descripcion, int pesoKg) {
        if (codigo == null || codigo.isBlank()) {
            throw new EcoRutaException("El código del lote es obligatorio.");
        }
        if (pesoKg <= 0) {
            throw new EcoRutaException("El peso del lote debe ser un entero positivo.");
        }
        this.codigo = codigo;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.pesoKg = pesoKg;
    }


    public abstract long calcularValor();

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getPesoKg() {
        return pesoKg;
    }
}
