package co.edu.uniquindio.universidad;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Entrega de materiales de un aportante. Compuesta por sus lotes.
public class Entrega {

    private final String codigo;
    private final LocalDate fecha;
    private final Aportante aportante;
    private final List<Lote> lotes = new ArrayList<>();
    private EstadoEntrega estado = EstadoEntrega.ABIERTA;
    private Comprobante comprobante;

    public Entrega(String codigo, LocalDate fecha, Aportante aportante) {
        if (codigo == null || codigo.isBlank()) {
            throw new EcoRutaException("El código de la entrega es obligatorio.");
        }
        if (fecha == null || aportante == null) {
            throw new EcoRutaException("La fecha y el aportante son obligatorios.");
        }
        this.codigo = codigo;
        this.fecha = fecha;
        this.aportante = aportante;
    }

    // Agrega un lote si la entrega está abierta y el código no se repite.
    public void agregarLote(Lote lote) {
        if (lote == null) {
            throw new EcoRutaException("El lote es obligatorio.");
        }
        if (estado != EstadoEntrega.ABIERTA) {
            throw new EcoRutaException("La entrega " + codigo + " ya fue aceptada; no admite más lotes.");
        }
        boolean repetido = lotes.stream().anyMatch(l -> l.getCodigo().equals(lote.getCodigo()));
        if (repetido) {
            throw new EcoRutaException("Ya existe un lote con código " + lote.getCodigo() + ".");
        }
        lotes.add(lote);
    }

    // suma
    public long calcularValorTotal() {
        long total = 0;
        for (Lote lote : lotes) {
            total += lote.calcularValor();
        }
        return total;
    }

    public int calcularPesoTotal() {
        int total = 0;
        for (Lote lote : lotes) {
            total += lote.getPesoKg();
        }
        return total;
    }

    /** Acepta la entrega y genera el comprobante. Rechaza si está vacía o ya aceptada. */
    public Comprobante aceptar() {
        if (estado == EstadoEntrega.ACEPTADA) {
            throw new EcoRutaException("La entrega " + codigo + " ya fue aceptada.");
        }
        if (lotes.isEmpty()) {
            throw new EcoRutaException("La entrega " + codigo + " está vacía; no puede aceptarse.");
        }
        Comprobante nuevo = new Comprobante(codigo, aportante.identificacion(),
                calcularPesoTotal(), calcularValorTotal());
        this.comprobante = nuevo;
        this.estado = EstadoEntrega.ACEPTADA;
        return nuevo;
    }

    /** Etiquetas de los lotes que las generan; solo disponibles en entregas aceptadas. */
    public List<String> getEtiquetas() {
        List<String> etiquetas = new ArrayList<>();
        if (estado == EstadoEntrega.ACEPTADA) {
            for (Lote lote : lotes) {
                if (lote instanceof Etiquetable e) {
                    etiquetas.add(e.generarEtiqueta());
                }
            }
        }
        return etiquetas;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Aportante getAportante() {
        return aportante;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    public List<Lote> getLotes() {
        return Collections.unmodifiableList(lotes);
    }

    public Optional<Comprobante> getComprobante() {
        return Optional.ofNullable(comprobante);
    }
}
