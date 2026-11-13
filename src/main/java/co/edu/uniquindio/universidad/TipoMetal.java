package co.edu.uniquindio.universidad;

/** Tipos de metal que recibe EcoRuta, con el valor reconocido por kilogramo. */
public enum TipoMetal {
    ALUMINIO(2200),
    ACERO(900);

    private final int valorPorKg;

    TipoMetal(int valorPorKg) {
        this.valorPorKg = valorPorKg;
    }

    public int getValorPorKg() {
        return valorPorKg;
    }
}
