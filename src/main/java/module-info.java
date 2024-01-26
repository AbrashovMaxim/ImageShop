module com.maxabrashov.imageshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;
    requires java.desktop;


    opens com.maxabrashov.imageshop to javafx.fxml;
    exports com.maxabrashov.imageshop;
    exports com.maxabrashov.imageshop.elements;
    opens com.maxabrashov.imageshop.elements to javafx.fxml;
}