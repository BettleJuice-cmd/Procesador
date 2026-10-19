module ni.uam.edu.procesador {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.uam.edu.procesador to javafx.fxml;
    exports ni.uam.edu.procesador;
}