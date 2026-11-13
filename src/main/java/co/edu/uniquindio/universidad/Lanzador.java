package co.edu.uniquindio.universidad;

import javafx.application.Application;

/**
 * Punto de entrada de la interfaz gráfica.
 *
 * Es una clase aparte (que NO extiende Application) por una razón técnica: si la clase con
 * main() extiende Application y JavaFX viene como librería Maven, Java muestra el error
 * "JavaFX runtime components are missing". Con este lanzador intermedio no ocurre.
 */
public class Lanzador {
    public static void main(String[] args) {
        Application.launch(EcoRutaApp.class, args);
    }
}
