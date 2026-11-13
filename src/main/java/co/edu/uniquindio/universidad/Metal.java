package co.edu.uniquindio.universidad;

// Metales: valor por kilogramo según el tipo (aluminio o acero).
public class Metal extends Lote implements Etiquetable {

    private final TipoMetal tipoMetal;

    public Metal(String codigo, String descripcion, int pesoKg, TipoMetal tipoMetal) {
        super(codigo, descripcion, pesoKg);
        if (tipoMetal == null) {
            throw new EcoRutaException("El tipo de metal es obligatorio.");
        }
        this.tipoMetal = tipoMetal;
    }

    public TipoMetal getTipoMetal() {
        return tipoMetal;
    }

    @Override
    public long calcularValor() {
        return (long) tipoMetal.getValorPorKg() * getPesoKg();
    }

    @Override
    public String generarEtiqueta() {
        return "Lote " + getCodigo() + " - Metal: " + tipoMetal;
    }
}
