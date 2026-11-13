package co.edu.uniquindio.universidad;

public class ResiduoElectronico extends Lote implements Etiquetable {

    private static final int VALOR_POR_KG = 1400;
    private static final int VALOR_POR_EQUIPO = 800;

    private final int equiposCompletos;

    public ResiduoElectronico(String codigo, String descripcion, int pesoKg, int equiposCompletos) {
        super(codigo, descripcion, pesoKg);
        if (equiposCompletos < 0) {
            throw new EcoRutaException("Los equipos completos no pueden ser negativos.");
        }
        this.equiposCompletos = equiposCompletos;
    }

    public int getEquiposCompletos() {
        return equiposCompletos;
    }

    @Override
    public long calcularValor() {
        return (long) VALOR_POR_KG * getPesoKg() + (long) VALOR_POR_EQUIPO * equiposCompletos;
    }

    @Override
    public String generarEtiqueta() {
        return "Lote " + getCodigo() + " - Equipos completos: " + equiposCompletos;
    }
}
