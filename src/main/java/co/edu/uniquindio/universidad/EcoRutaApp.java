package co.edu.uniquindio.universidad;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Interfaz gráfica de EcoRuta (JavaFX).
 *
 * Esta clase NO contiene reglas de negocio: solo muestra datos y llama a {@link CentroEcoRuta},
 * que es la misma lógica que usa el menú de consola.
 *
 * Estructura de la ventana:
 *   - Arriba: título.
 *   - Centro: 3 pestañas (Aportantes, Entregas, Pruebas).
 *   - Abajo: barra de estado con el resultado de la última acción.
 */
public class EcoRutaApp extends Application {

    private static final String VERDE = "#2e7d32";
    private static final String ROJO = "#c62828";
    private static final String ESTILO_PRIMARIO =
            "-fx-background-color: " + VERDE + "; -fx-text-fill: white; -fx-font-weight: bold;";

    /** Misma lógica de negocio que usa la consola. */
    private final CentroEcoRuta centro = new CentroEcoRuta();

    // Controles que necesitamos actualizar desde varios métodos.
    private final TableView<Aportante> tablaAportantes = new TableView<>();
    private final TableView<Entrega> tablaEntregas = new TableView<>();
    private final TableView<Lote> tablaLotes = new TableView<>();
    private final ComboBox<Aportante> cmbAportante = new ComboBox<>();
    private final Label lblResumen = new Label();
    private final TextArea txtDetalle = new TextArea();
    private final Button btnAgregarLote = new Button("Agregar lote…");
    private final Button btnAceptar = new Button("Aceptar entrega");
    private final Label lblEstado = new Label("Listo.");

    /** JavaFX llama a este método cuando la aplicación arranca: aquí se arma la ventana. */
    @Override
    public void start(Stage stage) {
        // Datos de ejemplo (los mismos que usa Main) para no empezar con todo vacío.
        centro.registrarAportante(new Aportante("1001", "Ana Torres", "ana@mail.com"));
        centro.registrarAportante(new Aportante("1002", "Comercial Verde", "verde@mail.com"));

        Label titulo = new Label("EcoRuta · Centro de acopio de reciclaje");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setStyle("-fx-background-color: " + VERDE + "; -fx-text-fill: white;"
                + " -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 12 16 12 16;");

        TabPane pestanas = new TabPane(crearTabAportantes(), crearTabEntregas(), crearTabPruebas());
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        lblEstado.setPadding(new Insets(6, 12, 6, 12));

        BorderPane raiz = new BorderPane(pestanas);
        raiz.setTop(titulo);
        raiz.setBottom(lblEstado);

        refrescar();

        stage.setTitle("EcoRuta");
        stage.setScene(new Scene(raiz, 980, 700));
        stage.setMinWidth(820);
        stage.setMinHeight(600);
        stage.show();
    }

    // ------------------------------------------------------------------------------------
    // Pestaña 1: Aportantes
    // ------------------------------------------------------------------------------------

    private Tab crearTabAportantes() {
        TextField txtId = new TextField();
        txtId.setPromptText("Ej: 1003");
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Juan Pérez");
        TextField txtCorreo = new TextField();
        txtCorreo.setPromptText("Ej: juan@mail.com");
        txtId.setPrefWidth(300);
        txtNombre.setPrefWidth(300);
        txtCorreo.setPrefWidth(300);

        Button btnRegistrar = new Button("Registrar aportante");
        btnRegistrar.setStyle(ESTILO_PRIMARIO);
        btnRegistrar.setOnAction(e -> {
            try {
                centro.registrarAportante(new Aportante(txtId.getText().trim(),
                        txtNombre.getText().trim(), txtCorreo.getText().trim()));
                String nombre = txtNombre.getText().trim();
                txtId.clear();
                txtNombre.clear();
                txtCorreo.clear();
                refrescar();
                exito("Aportante " + nombre + " registrado.");
            } catch (EcoRutaException ex) {
                rechazo(ex.getMessage());
            }
        });

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(8);
        formulario.addRow(0, new Label("Identificación:"), txtId);
        formulario.addRow(1, new Label("Nombre:"), txtNombre);
        formulario.addRow(2, new Label("Correo:"), txtCorreo);
        formulario.add(btnRegistrar, 1, 3);

        agregarColumna(tablaAportantes, "Identificación", 130, Aportante::identificacion);
        agregarColumna(tablaAportantes, "Nombre", 220, Aportante::nombre);
        agregarColumna(tablaAportantes, "Correo", 240, Aportante::correo);
        agregarColumna(tablaAportantes, "Valor acumulado", 160,
                a -> pesos(centro.valorAcumuladoPorAportante(a.identificacion())));
        tablaAportantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaAportantes.setPlaceholder(new Label("Aún no hay aportantes registrados."));

        VBox contenido = new VBox(12,
                subtitulo("Nuevo aportante"), formulario,
                subtitulo("Aportantes registrados"), tablaAportantes);
        VBox.setVgrow(tablaAportantes, Priority.ALWAYS);
        contenido.setPadding(new Insets(15));
        return new Tab("Aportantes", contenido);
    }

    // ------------------------------------------------------------------------------------
    // Pestaña 2: Entregas
    // ------------------------------------------------------------------------------------

    private Tab crearTabEntregas() {
        // --- Formulario "Nueva entrega" ---
        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("Ej: E01");
        txtCodigo.setPrefWidth(110);

        cmbAportante.setPromptText("Selecciona un aportante");
        cmbAportante.setPrefWidth(240);
        cmbAportante.setConverter(new StringConverter<>() {
            @Override
            public String toString(Aportante a) {
                return a == null ? "" : a.identificacion() + " - " + a.nombre();
            }

            @Override
            public Aportante fromString(String texto) {
                return null; // el combo no es editable
            }
        });

        DatePicker selectorFecha = new DatePicker(LocalDate.now());
        selectorFecha.setPrefWidth(140);

        Button btnCrear = new Button("Crear entrega");
        btnCrear.setStyle(ESTILO_PRIMARIO);
        btnCrear.setOnAction(e -> {
            Aportante aportante = cmbAportante.getValue();
            if (aportante == null) {
                rechazo("Selecciona un aportante.");
                return;
            }
            try {
                LocalDate fecha = selectorFecha.getValue() == null ? LocalDate.now() : selectorFecha.getValue();
                Entrega nueva = centro.crearEntrega(txtCodigo.getText().trim(), fecha, aportante.identificacion());
                txtCodigo.clear();
                refrescar();
                tablaEntregas.getSelectionModel().select(nueva);
                exito("Entrega " + nueva.getCodigo() + " creada. Ahora agrégale lotes.");
            } catch (EcoRutaException ex) {
                rechazo(ex.getMessage());
            }
        });

        HBox formulario = new HBox(10,
                new Label("Código:"), txtCodigo,
                new Label("Aportante:"), cmbAportante,
                new Label("Fecha:"), selectorFecha,
                btnCrear);
        formulario.setAlignment(Pos.CENTER_LEFT);

        // --- Tabla de entregas ---
        agregarColumna(tablaEntregas, "Código", 80, Entrega::getCodigo);
        agregarColumna(tablaEntregas, "Fecha", 100, en -> en.getFecha().toString());
        agregarColumna(tablaEntregas, "Aportante", 180, en -> en.getAportante().nombre());
        agregarColumna(tablaEntregas, "Estado", 90, en -> en.getEstado().name());
        agregarColumna(tablaEntregas, "Lotes", 60, en -> String.valueOf(en.getLotes().size()));
        agregarColumna(tablaEntregas, "Peso", 80, en -> en.calcularPesoTotal() + " kg");
        agregarColumna(tablaEntregas, "Valor", 110, en -> pesos(en.calcularValorTotal()));
        tablaEntregas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaEntregas.setPlaceholder(new Label("Aún no hay entregas. Crea la primera arriba."));
        tablaEntregas.setMinHeight(90);
        tablaEntregas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> mostrarDetalle(actual));

        // --- Tabla de lotes de la entrega seleccionada ---
        agregarColumna(tablaLotes, "Código", 80, Lote::getCodigo);
        agregarColumna(tablaLotes, "Tipo", 190, EcoRutaApp::describirTipo);
        agregarColumna(tablaLotes, "Descripción", 220, Lote::getDescripcion);
        agregarColumna(tablaLotes, "Peso", 80, l -> l.getPesoKg() + " kg");
        agregarColumna(tablaLotes, "Valor", 110, l -> pesos(l.calcularValor()));
        tablaLotes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaLotes.setPlaceholder(new Label("Esta entrega aún no tiene lotes."));
        tablaLotes.setItems(FXCollections.observableArrayList());
        tablaLotes.setMinHeight(90);

        // --- Botones y detalle ---
        btnAgregarLote.setOnAction(e -> {
            Entrega seleccionada = tablaEntregas.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                abrirDialogoLote(seleccionada);
            }
        });

        btnAceptar.setStyle(ESTILO_PRIMARIO);
        btnAceptar.setOnAction(e -> aceptarSeleccionada());

        HBox botones = new HBox(10, btnAgregarLote, btnAceptar);
        botones.setAlignment(Pos.CENTER_LEFT);

        lblResumen.setStyle("-fx-font-weight: bold;");
        txtDetalle.setEditable(false);
        txtDetalle.setPrefRowCount(7);
        txtDetalle.setStyle("-fx-font-family: 'Monospaced';");

        VBox contenido = new VBox(10,
                subtitulo("Nueva entrega"), formulario,
                subtitulo("Entregas (selecciona una para ver su detalle)"), tablaEntregas,
                subtitulo("Detalle de la entrega seleccionada"), lblResumen, tablaLotes, botones,
                subtitulo("Comprobante y etiquetas"), txtDetalle);
        VBox.setVgrow(tablaEntregas, Priority.ALWAYS);
        VBox.setVgrow(tablaLotes, Priority.ALWAYS);
        contenido.setPadding(new Insets(15));
        return new Tab("Entregas", contenido);
    }

    /** Muestra lotes, resumen y comprobante/etiquetas de la entrega elegida (o limpia si no hay). */
    private void mostrarDetalle(Entrega entrega) {
        if (entrega == null) {
            tablaLotes.setItems(FXCollections.observableArrayList());
            lblResumen.setText("Selecciona una entrega de la tabla de arriba.");
            txtDetalle.clear();
            btnAgregarLote.setDisable(true);
            btnAceptar.setDisable(true);
            return;
        }

        tablaLotes.setItems(FXCollections.observableArrayList(entrega.getLotes()));
        lblResumen.setText(String.format("Entrega %s · %s · %d lote(s) · %d kg · valor %s",
                entrega.getCodigo(), entrega.getEstado(), entrega.getLotes().size(),
                entrega.calcularPesoTotal(), pesos(entrega.calcularValorTotal())));

        boolean abierta = entrega.getEstado() == EstadoEntrega.ABIERTA;
        btnAgregarLote.setDisable(!abierta);
        btnAceptar.setDisable(!abierta);

        if (abierta) {
            txtDetalle.setText("La entrega está ABIERTA.\n"
                    + "Agrega los lotes y luego pulsa \"Aceptar entrega\" para generar\n"
                    + "el comprobante y las etiquetas.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        entrega.getComprobante().ifPresent(c -> sb
                .append("COMPROBANTE\n")
                .append("  Entrega   : ").append(c.codigoEntrega()).append('\n')
                .append("  Aportante : ").append(c.identificacionAportante()).append('\n')
                .append("  Peso total: ").append(c.pesoTotal()).append(" kg\n")
                .append("  Valor     : ").append(pesos(c.valorTotal())).append("\n\n"));
        sb.append("ETIQUETAS\n");
        var etiquetas = centro.consultarEtiquetas(entrega.getCodigo());
        if (etiquetas.isEmpty()) {
            sb.append("  (ningún lote de esta entrega genera etiqueta)\n");
        } else {
            etiquetas.forEach(et -> sb.append("  - ").append(et).append('\n'));
        }
        txtDetalle.setText(sb.toString());
    }

    private void aceptarSeleccionada() {
        Entrega seleccionada = tablaEntregas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "Una vez aceptada, la entrega no admite más lotes. ¿Deseas continuar?",
                ButtonType.YES, ButtonType.NO);
        confirmar.setTitle("Confirmar");
        confirmar.setHeaderText("Aceptar entrega " + seleccionada.getCodigo());
        if (confirmar.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }
        try {
            Comprobante c = centro.aceptarEntrega(seleccionada.getCodigo());
            refrescar();
            exito("Entrega " + c.codigoEntrega() + " aceptada. Valor total: " + pesos(c.valorTotal()));
        } catch (EcoRutaException ex) {
            rechazo(ex.getMessage());
        }
    }

    /**
     * Ventana emergente para agregar un lote. Si el lote es inválido (código repetido, peso 0...)
     * la ventana NO se cierra: muestra el error en rojo para que el usuario lo corrija.
     */
    private void abrirDialogoLote(Entrega entrega) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Agregar lote");
        dialogo.setHeaderText("Nuevo lote para la entrega " + entrega.getCodigo());
        ButtonType btnOk = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Papel y cartón", "Metal", "Residuo electrónico"));
        cmbTipo.getSelectionModel().selectFirst();
        TextField txtCodigoLote = new TextField();
        txtCodigoLote.setPromptText("Ej: L01");
        TextField txtDescripcion = new TextField();
        txtDescripcion.setPromptText("Ej: Cajas de cartón");
        TextField txtPeso = new TextField();
        txtPeso.setPromptText("Entero positivo");

        // Campos que dependen del tipo de lote.
        Label lblClasificado = new Label("Clasificado:");
        CheckBox chkClasificado = new CheckBox("Sí, el material viene clasificado");
        Label lblMetal = new Label("Tipo de metal:");
        ComboBox<TipoMetal> cmbMetal = new ComboBox<>(FXCollections.observableArrayList(TipoMetal.values()));
        cmbMetal.getSelectionModel().selectFirst();
        Label lblEquipos = new Label("Equipos completos:");
        TextField txtEquipos = new TextField("0");

        Runnable ajustarCampos = () -> {
            int tipo = cmbTipo.getSelectionModel().getSelectedIndex();
            mostrar(tipo == 0, lblClasificado, chkClasificado);
            mostrar(tipo == 1, lblMetal, cmbMetal);
            mostrar(tipo == 2, lblEquipos, txtEquipos);
        };
        cmbTipo.setOnAction(e -> ajustarCampos.run());
        ajustarCampos.run();

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: " + ROJO + ";");
        lblError.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Tipo de lote:"), cmbTipo);
        grid.addRow(1, new Label("Código del lote:"), txtCodigoLote);
        grid.addRow(2, new Label("Descripción:"), txtDescripcion);
        grid.addRow(3, new Label("Peso (kg):"), txtPeso);
        grid.addRow(4, lblClasificado, chkClasificado);
        grid.addRow(5, lblMetal, cmbMetal);
        grid.addRow(6, lblEquipos, txtEquipos);

        VBox contenido = new VBox(10, grid, lblError);
        contenido.setPrefWidth(430);
        dialogo.getDialogPane().setContent(contenido);

        long[] valorAgregado = new long[1];
        Button botonAgregar = (Button) dialogo.getDialogPane().lookupButton(btnOk);
        botonAgregar.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                String codigo = txtCodigoLote.getText().trim();
                String descripcion = txtDescripcion.getText().trim();
                int peso = Integer.parseInt(txtPeso.getText().trim());
                Lote lote = switch (cmbTipo.getSelectionModel().getSelectedIndex()) {
                    case 0 -> new PapelCarton(codigo, descripcion, peso, chkClasificado.isSelected());
                    case 1 -> new Metal(codigo, descripcion, peso, cmbMetal.getValue());
                    default -> new ResiduoElectronico(codigo, descripcion, peso,
                            Integer.parseInt(txtEquipos.getText().trim()));
                };
                centro.agregarLote(entrega.getCodigo(), lote);
                valorAgregado[0] = lote.calcularValor();
            } catch (NumberFormatException ex) {
                lblError.setText("El peso y los equipos deben ser números enteros (sin decimales).");
                ev.consume(); // evita que se cierre la ventana
            } catch (EcoRutaException ex) {
                lblError.setText(ex.getMessage());
                ev.consume();
            }
        });

        Optional<ButtonType> resultado = dialogo.showAndWait();
        if (resultado.isPresent() && resultado.get() == btnOk) {
            refrescar();
            exito("Lote agregado a " + entrega.getCodigo() + ". Valor del lote: " + pesos(valorAgregado[0]));
        }
    }

    // ------------------------------------------------------------------------------------
    // Pestaña 3: Pruebas funcionales
    // ------------------------------------------------------------------------------------

    private Tab crearTabPruebas() {
        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setStyle("-fx-font-family: 'Monospaced';");
        salida.setPromptText("Aquí aparecerá el resultado de las pruebas.");

        Button btnEjecutar = new Button("Ejecutar pruebas funcionales");
        btnEjecutar.setStyle(ESTILO_PRIMARIO);
        btnEjecutar.setOnAction(e -> {
            // PruebasFuncionales escribe con System.out; lo capturamos para mostrarlo en pantalla.
            PrintStream consolaOriginal = System.out;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int aprobadas = 0;
            try (PrintStream captura = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
                System.setOut(captura);
                aprobadas = PruebasFuncionales.ejecutarTodas();
            } finally {
                System.setOut(consolaOriginal);
            }
            salida.setText(buffer.toString(StandardCharsets.UTF_8));
            if (aprobadas == 5) {
                exito("Las 5 pruebas funcionales fueron aprobadas.");
            } else {
                lblEstado.setStyle("-fx-text-fill: " + ROJO + "; -fx-font-weight: bold;");
                lblEstado.setText("✖ Solo " + aprobadas + " de 5 pruebas fueron aprobadas.");
            }
        });

        VBox contenido = new VBox(12,
                subtitulo("Pruebas funcionales del enunciado"),
                new Label("Usan datos propios; no afectan los aportantes ni las entregas de esta ventana."),
                btnEjecutar, salida);
        VBox.setVgrow(salida, Priority.ALWAYS);
        contenido.setPadding(new Insets(15));
        return new Tab("Pruebas", contenido);
    }

    // ------------------------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------------------------

    /** Vuelve a cargar las tablas con los datos actuales del centro. */
    private void refrescar() {
        tablaAportantes.setItems(FXCollections.observableArrayList(centro.getAportantes()));

        Aportante aportanteElegido = cmbAportante.getValue();
        cmbAportante.setItems(FXCollections.observableArrayList(centro.getAportantes()));
        cmbAportante.setValue(aportanteElegido);

        Entrega entregaElegida = tablaEntregas.getSelectionModel().getSelectedItem();
        tablaEntregas.setItems(FXCollections.observableArrayList(centro.getEntregas()));
        if (entregaElegida != null) {
            tablaEntregas.getSelectionModel().select(entregaElegida);
        }
        mostrarDetalle(tablaEntregas.getSelectionModel().getSelectedItem());
    }

    /** Agrega una columna de texto a una tabla; "valor" indica qué mostrar en cada fila. */
    private static <T> void agregarColumna(TableView<T> tabla, String titulo, int ancho,
                                           Function<T, String> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(celda -> new SimpleStringProperty(valor.apply(celda.getValue())));
        tabla.getColumns().add(columna);
    }

    private static String describirTipo(Lote lote) {
        if (lote instanceof PapelCarton p) {
            return "Papel y cartón" + (p.isClasificado() ? " (clasificado)" : "");
        }
        if (lote instanceof Metal m) {
            return "Metal (" + m.getTipoMetal() + ")";
        }
        if (lote instanceof ResiduoElectronico r) {
            return "Electrónico (" + r.getEquiposCompletos() + " equipos)";
        }
        return "Lote";
    }

    private static void mostrar(boolean visible, Node... nodos) {
        for (Node n : nodos) {
            n.setVisible(visible);
            n.setManaged(visible); // managed=false: no ocupa espacio cuando está oculto
        }
    }

    private static Label subtitulo(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + VERDE + ";");
        return l;
    }

    private static String pesos(long valor) {
        return String.format(Locale.forLanguageTag("es-CO"), "$%,d", valor);
    }

    private void exito(String mensaje) {
        lblEstado.setStyle("-fx-text-fill: " + VERDE + "; -fx-font-weight: bold;");
        lblEstado.setText("✔ " + mensaje);
    }

    private void rechazo(String mensaje) {
        lblEstado.setStyle("-fx-text-fill: " + ROJO + "; -fx-font-weight: bold;");
        lblEstado.setText("✖ " + mensaje);
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.setTitle("EcoRuta");
        alerta.setHeaderText("Operación rechazada");
        alerta.showAndWait();
    }
}
