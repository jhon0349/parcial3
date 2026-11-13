package co.edu.uniquindio.universidad;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.time.LocalDate;
import java.util.Locale;
import java.util.Scanner;

/** Menú interactivo para demostrar las funcionalidades de EcoRuta. */
public class Main {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        CentroEcoRuta centro = new CentroEcoRuta();
        centro.registrarAportante(new Aportante("1001", "Ana Torres", "ana@mail.com"));
        centro.registrarAportante(new Aportante("1002", "Comercial Verde", "verde@mail.com"));

        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> listarAportantes(centro);
                    case 2 -> crearEntrega(centro);
                    case 3 -> agregarLote(centro);
                    case 4 -> aceptarEntrega(centro);
                    case 5 -> consultarComprobante(centro);
                    case 6 -> consultarEtiquetas(centro);
                    case 7 -> consultarAcumulado(centro);
                    case 8 -> PruebasFuncionales.ejecutarTodas();
                    case 0 -> System.out.println("¡Hasta luego!");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (EcoRutaException e) {
                System.out.println("OPERACIÓN RECHAZADA: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("""

                ===== EcoRuta =====
                1. Listar aportantes
                2. Crear entrega
                3. Agregar lote a una entrega
                4. Aceptar entrega
                5. Consultar comprobante por código de entrega
                6. Consultar etiquetas por código de entrega
                7. Consultar valor acumulado por aportante
                8. Ejecutar pruebas funcionales
                0. Salir""");
    }

    private static void listarAportantes(CentroEcoRuta centro) {
        centro.getAportantes().forEach(a ->
                System.out.println(a.identificacion() + " | " + a.nombre() + " | " + a.correo()));
    }

    private static void crearEntrega(CentroEcoRuta centro) {
        String codigo = leerTexto("Código de la entrega: ");
        String id = leerTexto("Identificación del aportante: ");
        Entrega e = centro.crearEntrega(codigo, LocalDate.now(), id);
        System.out.println("Entrega " + e.getCodigo() + " creada en estado " + e.getEstado() + ".");
    }

    private static void agregarLote(CentroEcoRuta centro) {
        String entrega = leerTexto("Código de la entrega: ");
        System.out.println("Tipo de lote: 1. Papel y cartón  2. Metal  3. Residuo electrónico");
        int tipo = leerEntero("Tipo: ");
        String codigo = leerTexto("Código del lote: ");
        String desc = leerTexto("Descripción: ");
        int peso = leerEntero("Peso (kg): ");
        Lote lote;
        switch (tipo) {
            case 1 -> {
                boolean clasificado = leerTexto("¿Clasificado? (s/n): ").equalsIgnoreCase("s");
                lote = new PapelCarton(codigo, desc, peso, clasificado);
            }
            case 2 -> {
                int t = leerEntero("Metal: 1. ALUMINIO  2. ACERO: ");
                if (t != 1 && t != 2) throw new EcoRutaException("Tipo de metal no válido.");
                lote = new Metal(codigo, desc, peso, t == 1 ? TipoMetal.ALUMINIO : TipoMetal.ACERO);
            }
            case 3 -> lote = new ResiduoElectronico(codigo, desc, peso,
                    leerEntero("Equipos completos: "));
            default -> throw new EcoRutaException("Tipo de lote no válido.");
        }
        centro.agregarLote(entrega, lote);
        System.out.println("Lote " + codigo + " agregado. Valor del lote: " + pesos(lote.calcularValor()));
    }

    private static void aceptarEntrega(CentroEcoRuta centro) {
        Comprobante c = centro.aceptarEntrega(leerTexto("Código de la entrega: "));
        imprimirComprobante(c);
    }

    private static void consultarComprobante(CentroEcoRuta centro) {
        String codigo = leerTexto("Código de la entrega: ");
        centro.consultarComprobante(codigo).ifPresentOrElse(Main::imprimirComprobante,
                () -> System.out.println("La entrega no tiene comprobante (no está aceptada)."));
    }

    private static void consultarEtiquetas(CentroEcoRuta centro) {
        var etiquetas = centro.consultarEtiquetas(leerTexto("Código de la entrega: "));
        if (etiquetas.isEmpty()) {
            System.out.println("Sin etiquetas (la entrega no está aceptada o no tiene lotes etiquetables).");
        } else {
            etiquetas.forEach(System.out::println);
        }
    }

    private static void consultarAcumulado(CentroEcoRuta centro) {
        String id = leerTexto("Identificación del aportante: ");
        System.out.println("Valor acumulado: " + pesos(centro.valorAcumuladoPorAportante(id)));
    }

    private static void imprimirComprobante(Comprobante c) {
        System.out.println("COMPROBANTE | entrega " + c.codigoEntrega() + " | aportante "
                + c.identificacionAportante() + " | " + c.pesoTotal() + " kg | " + pesos(c.valorTotal()));
    }

    private static String pesos(long valor) {
        return String.format(Locale.forLanguageTag("es-CO"), "$%,d", valor);
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return SC.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        try {
            return Integer.parseInt(SC.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
