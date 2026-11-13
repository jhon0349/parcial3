package co.edu.uniquindio.universidad;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Servicio principal: gestiona en memoria aportantes y entregas. */
public class CentroEcoRuta {

    private final Map<String, Aportante> aportantes = new LinkedHashMap<>();
    private final Map<String, Entrega> entregas = new LinkedHashMap<>();

    public void registrarAportante(Aportante aportante) {
        if (aportantes.containsKey(aportante.identificacion())) {
            throw new EcoRutaException("Ya existe un aportante con identificación "
                    + aportante.identificacion() + ".");
        }
        aportantes.put(aportante.identificacion(), aportante);
    }

    public Entrega crearEntrega(String codigo, LocalDate fecha, String identificacionAportante) {
        if (codigo == null || codigo.isBlank()) {
            throw new EcoRutaException("El código de la entrega es obligatorio.");
        }
        if (entregas.containsKey(codigo)) {
            throw new EcoRutaException("Ya existe una entrega con código " + codigo + ".");
        }
        Aportante aportante = aportantes.get(identificacionAportante);
        if (aportante == null) {
            throw new EcoRutaException("Aportante desconocido: " + identificacionAportante + ".");
        }
        Entrega entrega = new Entrega(codigo, fecha, aportante);
        entregas.put(codigo, entrega);
        return entrega;
    }

    public void agregarLote(String codigoEntrega, Lote lote) {
        obtenerEntrega(codigoEntrega).agregarLote(lote);
    }

    public Comprobante aceptarEntrega(String codigoEntrega) {
        return obtenerEntrega(codigoEntrega).aceptar();
    }

    public Optional<Comprobante> consultarComprobante(String codigoEntrega) {
        return obtenerEntrega(codigoEntrega).getComprobante();
    }

    public List<String> consultarEtiquetas(String codigoEntrega) {
        return obtenerEntrega(codigoEntrega).getEtiquetas();
    }

    /** Suma solo comprobantes de entregas aceptadas; si no hay, devuelve 0. */
    public long valorAcumuladoPorAportante(String identificacion) {
        long total = 0;
        for (Entrega e : entregas.values()) {
            if (e.getEstado() == EstadoEntrega.ACEPTADA
                    && e.getAportante().identificacion().equals(identificacion)) {
                total += e.getComprobante().map(Comprobante::valorTotal).orElse(0L);
            }
        }
        return total;
    }

    public Optional<Entrega> buscarEntrega(String codigo) {
        return Optional.ofNullable(entregas.get(codigo));
    }

    public Collection<Aportante> getAportantes() {
        return Collections.unmodifiableCollection(aportantes.values());
    }

    public Collection<Entrega> getEntregas() {
        return Collections.unmodifiableCollection(entregas.values());
    }

    private Entrega obtenerEntrega(String codigo) {
        Entrega e = entregas.get(codigo);
        if (e == null) {
            throw new EcoRutaException("La entrega " + codigo + " no existe.");
        }
        return e;
    }
}
