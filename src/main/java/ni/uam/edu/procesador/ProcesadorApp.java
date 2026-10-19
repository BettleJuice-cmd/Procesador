package ni.uam.edu.procesador;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProcesadorApp extends Application {
    private final ContadorSeguro contador = new ContadorSeguro();
    private Task<Void> tarea1;
    private Task<Void> tarea2;
    private final Label total = new Label("Procesadas: 0");
    private final Label estado = new Label("Sin iniciar");
    private final ProgressBar barra1 = new ProgressBar(0);
    private final ProgressBar barra2 = new ProgressBar(0);
    private final Button iniciar = new Button("Iniciar");
    private final Button cancelar = new Button("Cancelar");

    private static class ContadorSeguro {
        private int valor;
        public synchronized int incrementar() { return ++valor; }
    }

    private Task<Void> crearTarea(String operador) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 1; i <= 20; i++) {
                    if (isCancelled()) break;
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        if (isCancelled()) break;
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                    if (isCancelled()) break;
                    int actual = contador.incrementar();
                    updateProgress(i, 20);
                    Platform.runLater(() -> total.setText("Procesadas: " + actual));
                }
                return null;
            }
        };
    }

    @Override
    public void start(Stage ventana) {
        cancelar.setDisable(true);
        iniciar.setOnAction(e -> {
            iniciar.setDisable(true);
            cancelar.setDisable(false);
            tarea1 = crearTarea("Operador A");
            tarea2 = crearTarea("Operador B");
            barra1.progressProperty().bind(tarea1.progressProperty());
            barra2.progressProperty().bind(tarea2.progressProperty());
            estado.setText("Procesando...");
            Runnable actualizarEstado = () -> {
                if (tarea1.isDone() && tarea2.isDone()) {
                    cancelar.setDisable(true);
                    estado.setText("Ambas tareas finalizaron o se cancelaron");
                }
            };
            tarea1.setOnSucceeded(ev -> actualizarEstado.run());
            tarea2.setOnSucceeded(ev -> actualizarEstado.run());
            tarea1.setOnCancelled(ev -> actualizarEstado.run());
            tarea2.setOnCancelled(ev -> actualizarEstado.run());
            tarea1.setOnFailed(ev -> actualizarEstado.run());
            tarea2.setOnFailed(ev -> actualizarEstado.run());
            Thread hilo1 = new Thread(tarea1, "operador-a");
            Thread hilo2 = new Thread(tarea2, "operador-b");
            hilo1.setDaemon(true);
            hilo2.setDaemon(true);
            hilo1.start();
            hilo2.start();
        });
        cancelar.setOnAction(e -> {
            if (tarea1 != null) tarea1.cancel(true);
            if (tarea2 != null) tarea2.cancel(true);
            cancelar.setDisable(true);
            estado.setText("Cancelación solicitada...");
        });
        VBox raiz = new VBox(12, total, estado,
                new Label("Operador A"), barra1,
                new Label("Operador B"), barra2, iniciar, cancelar);
        raiz.setStyle("-fx-padding: 20;");
        ventana.setScene(new Scene(raiz, 380, 300));
        ventana.setTitle("Procesamiento concurrente");
        ventana.setOnCloseRequest(e -> {
            if (tarea1 != null) tarea1.cancel(true);
            if (tarea2 != null) tarea2.cancel(true);
        });
        ventana.show();
    }
    {
}
    public static void main(String[] args) { launch(args); }
}
