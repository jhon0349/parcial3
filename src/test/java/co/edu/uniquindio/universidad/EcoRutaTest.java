package co.edu.uniquindio.universidad;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EcoRutaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 10);
    private CentroEcoRuta centro;

    @BeforeEach
    void preparar() {
        centro = new CentroEcoRuta();
        centro.registrarAportante(new Aportante("1001", "Ana Torres", "ana@mail.com"));
        centro.registrarAportante(new Aportante("1002", "Comercial Verde", "verde@mail.com"));
    }

    private void cargarEntregaDelEnunciado(String codigo, String aportante) {
        centro.crearEntrega(codigo, FECHA, aportante);
        centro.agregarLote(codigo, new PapelCarton("L01", "Papel", 10, true));
        centro.agregarLote(codigo, new Metal("L02", "Latas", 5, TipoMetal.ALUMINIO));
        centro.agregarLote(codigo, new ResiduoElectronico("L03", "PCs", 4, 2));
    }

    // ---------- Prueba 1 ----------
    @Test
    void prueba1_registroDeEntrega() {
        Entrega e = centro.crearEntrega("E01", FECHA, "1001");
        assertEquals(EstadoEntrega.ABIERTA, e.getEstado());
        assertTrue(e.getLotes().isEmpty());
        assertTrue(e.getComprobante().isEmpty());

        assertThrows(EcoRutaException.class, () -> centro.crearEntrega("E01", FECHA, "1001"));
        assertThrows(EcoRutaException.class, () -> centro.crearEntrega("E02", FECHA, "9999"));

        assertSame(e, centro.buscarEntrega("E01").orElseThrow());
        assertTrue(centro.buscarEntrega("E02").isEmpty());
        assertEquals(1, centro.getEntregas().size());
    }

    // ---------- Prueba 2 ----------
    @Test
    void prueba2_validacionDeLotes() {
        centro.crearEntrega("E01", FECHA, "1001");
        centro.agregarLote("E01", new PapelCarton("L01", "Cajas", 5, false));

        assertThrows(EcoRutaException.class,
                () -> centro.agregarLote("E01", new PapelCarton("L01", "Repetido", 3, false)));
        assertThrows(EcoRutaException.class,
                () -> centro.agregarLote("E01", new PapelCarton("L02", "Sin peso", 0, false)));

        Entrega e = centro.buscarEntrega("E01").orElseThrow();
        assertEquals(1, e.getLotes().size());
        assertEquals("L01", e.getLotes().get(0).getCodigo());
    }

    // ---------- Prueba 3 ----------
    @Test
    void prueba3_rechazoDeEntregaVacia() {
        centro.crearEntrega("E01", FECHA, "1001");
        assertThrows(EcoRutaException.class, () -> centro.aceptarEntrega("E01"));

        Entrega e = centro.buscarEntrega("E01").orElseThrow();
        assertEquals(EstadoEntrega.ABIERTA, e.getEstado());
        assertTrue(e.getComprobante().isEmpty());
    }

    // ---------- Prueba 4 ----------
    @Test
    void prueba4_aceptacionYCalculoConTresTiposDeLote() {
        cargarEntregaDelEnunciado("E01", "1001");
        Entrega e = centro.buscarEntrega("E01").orElseThrow();

        assertEquals(6000, e.getLotes().get(0).calcularValor());
        assertEquals(11000, e.getLotes().get(1).calcularValor());
        assertEquals(7200, e.getLotes().get(2).calcularValor());

        Comprobante c = centro.aceptarEntrega("E01");
        assertEquals(EstadoEntrega.ACEPTADA, e.getEstado());
        assertEquals(new Comprobante("E01", "1001", 19, 24200), c);
        assertEquals(c, centro.consultarComprobante("E01").orElseThrow());
    }

    // ---------- Prueba 5 ----------
    @Test
    void prueba5_consultaDeEtiquetas() {
        cargarEntregaDelEnunciado("E01", "1001");
        centro.aceptarEntrega("E01");

        List<String> etiquetas = centro.consultarEtiquetas("E01");
        assertEquals(2, etiquetas.size());
        assertTrue(etiquetas.get(0).contains("L02") && etiquetas.get(0).contains("ALUMINIO"));
        assertTrue(etiquetas.get(1).contains("L03") && etiquetas.get(1).contains("2"));
    }

    // ---------- Pruebas adicionales ----------
    @Test
    void entregaAceptadaNoAdmiteMasLotesNiSegundaAceptacion() {
        cargarEntregaDelEnunciado("E01", "1001");
        centro.aceptarEntrega("E01");

        assertThrows(EcoRutaException.class,
                () -> centro.agregarLote("E01", new PapelCarton("L09", "Tarde", 1, false)));
        assertThrows(EcoRutaException.class, () -> centro.aceptarEntrega("E01"));
        assertEquals(3, centro.buscarEntrega("E01").orElseThrow().getLotes().size());
    }

    @Test
    void aceptarEntregaInexistenteSeRechaza() {
        assertThrows(EcoRutaException.class, () -> centro.aceptarEntrega("NOPE"));
    }

    @Test
    void valorDeAceroYPapelSinClasificar() {
        assertEquals(4500, new Metal("M1", "Varilla", 5, TipoMetal.ACERO).calcularValor());
        assertEquals(5000, new PapelCarton("P1", "Periódico", 10, false).calcularValor());
        assertEquals(5600, new ResiduoElectronico("R1", "Cables", 4, 0).calcularValor());
    }

    @Test
    void papelNoGeneraEtiquetaYMetalYElectronicoSi() {
        assertFalse(new PapelCarton("P1", "x", 1, true) instanceof Etiquetable);
        assertEquals("Lote M1 - Metal: ACERO", new Metal("M1", "x", 1, TipoMetal.ACERO).generarEtiqueta());
    }

    @Test
    void etiquetasVaciasMientrasLaEntregaEstaAbierta() {
        cargarEntregaDelEnunciado("E01", "1001");
        assertTrue(centro.consultarEtiquetas("E01").isEmpty());
    }

    @Test
    void valoresInvalidosSeRechazan() {
        assertThrows(EcoRutaException.class, () -> new ResiduoElectronico("R1", "x", 1, -1));
        assertThrows(EcoRutaException.class, () -> new Metal("M1", "x", 1, null));
        assertThrows(EcoRutaException.class, () -> new Metal("M1", "x", -3, TipoMetal.ACERO));
    }

    @Test
    void acumuladoPorAportanteSoloSumaEntregasAceptadas() {
        assertEquals(0, centro.valorAcumuladoPorAportante("1001"));   // sin comprobantes
        assertEquals(0, centro.valorAcumuladoPorAportante("NO-EXISTE"));

        cargarEntregaDelEnunciado("E01", "1001");
        assertEquals(0, centro.valorAcumuladoPorAportante("1001"));   // abierta: no cuenta
        centro.aceptarEntrega("E01");

        centro.crearEntrega("E02", FECHA, "1001");
        centro.agregarLote("E02", new Metal("L01", "Varilla", 10, TipoMetal.ACERO)); // 9.000
        centro.aceptarEntrega("E02");

        centro.crearEntrega("E03", FECHA, "1001");                      // abierta: no cuenta
        centro.agregarLote("E03", new PapelCarton("L01", "x", 100, true));

        assertEquals(24200 + 9000, centro.valorAcumuladoPorAportante("1001"));
        assertEquals(0, centro.valorAcumuladoPorAportante("1002"));
    }

    @Test
    void comprobanteEsInmutableYNoCambiaTrasAceptar() {
        cargarEntregaDelEnunciado("E01", "1001");
        Comprobante c = centro.aceptarEntrega("E01");
        assertEquals("E01", c.codigoEntrega());
        assertEquals("1001", c.identificacionAportante());
        assertEquals(19, c.pesoTotal());
        assertEquals(24200, c.valorTotal());
    }
}
