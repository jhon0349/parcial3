package co.edu.uniquindio.universidad;

public class PapelCarton extends Lote {

    private static final int VALOR_POR_KG = 500;
    private static final int BONO_CLASIFICADO_POR_KG = 100;

    private final boolean clasificado;

    public PapelCarton(String codigo, String descripcion, int pesoKg, boolean clasificado) {
        super(codigo, descripcion, pesoKg);
        this.clasificado = clasificado;
    }

    public boolean isClasificado() {
        return clasificado;
    }

    @Override
    public long calcularValor() {
        long valorKg = VALOR_POR_KG + (clasificado ? BONO_CLASIFICADO_POR_KG : 0);
        return valorKg * getPesoKg();
    }
}