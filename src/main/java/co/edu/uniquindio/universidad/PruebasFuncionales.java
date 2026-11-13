package co.edu.uniquindio.universidad;


import java.time.LocalDate;

/** Las 5 pruebas funcionales del enunciado: muestran entrada, resultado esperado y obtenido. */
public class PruebasFuncionales {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 10);

    /** Ejecuta todas las pruebas y devuelve cuántas pasaron. */
    public static int ejecutarTodas() {
        boolean[] resultados = {
                prueba1RegistroEntrega(),
                prueba2ValidacionLotes(),
                prueba3EntregaVacia(),
                prueba4AceptacionYCalculo(),
                prueba5Etiquetas()
        };
        int ok = 0;
        for (boolean r : resultados) {
            if (r) ok++;
        }
        System.out.println("\n=== Resumen: " + ok + " de " + resultados.length + " pruebas aprobadas ===");
        return ok;
    }

    private static CentroEcoRuta nuevoCentro() {
        CentroEcoRuta centro = new CentroEcoRuta();
        centro.registrarAportante(new Aportante("1001", "Ana Torres", "ana@mail.com"));
        return centro;
    }

    private static boolean rechaza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (EcoRutaException e) {
            return true;
        }
    }

    private static boolean reportar(String titulo, String entrada, String esperado,
                                    String obtenido, boolean aprobada) {
        System.out.println("\n--- " + titulo + " ---");
        System.out.println("Entrada  : " + entrada);
        System.out.println("Esperado : " + esperado);
        System.out.println("Obtenido : " + obtenido);
        System.out.println("Resultado: " + (aprobada ? "APROBADA" : "FALLIDA"));
        return aprobada;
    }

    public static boolean prueba1RegistroEntrega() {
        CentroEcoRuta c = nuevoCentro();
        Entrega e01 = c.crearEntrega("E01", FECHA, "1001");
        boolean creadaOk = e01.getEstado() == EstadoEntrega.ABIERTA
                && e01.getLotes().isEmpty() && e01.getComprobante().isEmpty();
        boolean repetidaRechazada = rechaza(() -> c.crearEntrega("E01", FECHA, "1001"));
        boolean desconocidoRechazado = rechaza(() -> c.crearEntrega("E02", FECHA, "9999"));
        boolean conservada = c.buscarEntrega("E01").orElse(null) == e01
                && c.getEntregas().size() == 1;
        boolean ok = creadaOk && repetidaRechazada && desconocidoRechazado && conservada;
        return reportar("Prueba 1: Registro de una entrega",
                "crear E01 (aportante 1001); crear E01 otra vez; crear E02 con aportante 9999",
                "E01 ABIERTA, 0 lotes, sin comprobante; las otras 2 operaciones rechazadas; E01 conservada",
                "E01 " + e01.getEstado() + ", " + e01.getLotes().size() + " lotes, comprobante="
                        + e01.getComprobante().isPresent() + "; repetida rechazada=" + repetidaRechazada
                        + "; desconocido rechazado=" + desconocidoRechazado + "; entregas=" + c.getEntregas().size(),
                ok);
    }

    public static boolean prueba2ValidacionLotes() {
        CentroEcoRuta c = nuevoCentro();
        c.crearEntrega("E01", FECHA, "1001");
        c.agregarLote("E01", new PapelCarton("L01", "Cajas", 5, false));
        boolean repetidoRechazado = rechaza(() ->
                c.agregarLote("E01", new PapelCarton("L01", "Otro", 3, false)));
        boolean pesoCeroRechazado = rechaza(() ->
                c.agregarLote("E01", new PapelCarton("L02", "Sin peso", 0, false)));
        Entrega e = c.buscarEntrega("E01").orElseThrow();
        boolean ok = repetidoRechazado && pesoCeroRechazado && e.getLotes().size() == 1
                && e.getLotes().get(0).getCodigo().equals("L01");
        return reportar("Prueba 2: Validación de lotes",
                "agregar L01 (5 kg); agregar L01 otra vez; agregar L02 con 0 kg",
                "ambas operaciones rechazadas; la entrega conserva solo L01",
                "repetido rechazado=" + repetidoRechazado + "; peso 0 rechazado=" + pesoCeroRechazado
                        + "; lotes=" + e.getLotes().size(),
                ok);
    }

    public static boolean prueba3EntregaVacia() {
        CentroEcoRuta c = nuevoCentro();
        c.crearEntrega("E01", FECHA, "1001");
        boolean rechazada = rechaza(() -> c.aceptarEntrega("E01"));
        Entrega e = c.buscarEntrega("E01").orElseThrow();
        boolean ok = rechazada && e.getEstado() == EstadoEntrega.ABIERTA && e.getComprobante().isEmpty();
        return reportar("Prueba 3: Rechazo de una entrega vacía",
                "aceptar E01 sin lotes",
                "operación rechazada; estado ABIERTA; sin comprobante",
                "rechazada=" + rechazada + "; estado=" + e.getEstado()
                        + "; comprobante=" + e.getComprobante().isPresent(),
                ok);
    }

    /** Construye la entrega aceptada que usan las pruebas 4 y 5. */
    private static CentroEcoRuta centroConEntregaAceptada() {
        CentroEcoRuta c = nuevoCentro();
        c.crearEntrega("E01", FECHA, "1001");
        c.agregarLote("E01", new PapelCarton("L01", "Papel clasificado", 10, true));
        c.agregarLote("E01", new Metal("L02", "Latas", 5, TipoMetal.ALUMINIO));
        c.agregarLote("E01", new ResiduoElectronico("L03", "Computadores", 4, 2));
        c.aceptarEntrega("E01");
        return c;
    }

    public static boolean prueba4AceptacionYCalculo() {
        CentroEcoRuta c = centroConEntregaAceptada();
        Entrega e = c.buscarEntrega("E01").orElseThrow();
        Comprobante comp = e.getComprobante().orElse(null);
        long v1 = e.getLotes().get(0).calcularValor();
        long v2 = e.getLotes().get(1).calcularValor();
        long v3 = e.getLotes().get(2).calcularValor();
        boolean ok = e.getEstado() == EstadoEntrega.ACEPTADA && comp != null
                && comp.pesoTotal() == 19 && comp.valorTotal() == 24200
                && v1 == 6000 && v2 == 11000 && v3 == 7200;
        return reportar("Prueba 4: Aceptación y cálculo con los tres tipos de lote",
                "10 kg papel clasificado, 5 kg aluminio, 4 kg electrónicos con 2 equipos; aceptar",
                "ACEPTADA; 19 kg; $24.200; lotes $6.000, $11.000 y $7.200",
                e.getEstado() + "; " + (comp == null ? "sin comprobante" : comp.pesoTotal() + " kg; $"
                        + comp.valorTotal()) + "; lotes $" + v1 + ", $" + v2 + ", $" + v3,
                ok);
    }

    public static boolean prueba5Etiquetas() {
        CentroEcoRuta c = centroConEntregaAceptada();
        var etiquetas = c.consultarEtiquetas("E01");
        boolean ok = etiquetas.size() == 2
                && etiquetas.get(0).contains("L02") && etiquetas.get(0).contains("ALUMINIO")
                && etiquetas.get(1).contains("L03") && etiquetas.get(1).contains("2");
        return reportar("Prueba 5: Consulta de etiquetas",
                "consultar etiquetas de E01 (aceptada en la prueba 4)",
                "exactamente 2: metal con código y ALUMINIO; electrónico con código y 2 equipos",
                etiquetas.size() + " etiquetas: " + etiquetas,
                ok);
    }
}
