package com.maxabrashov.imageshop;

import com.maxabrashov.imageshop.elements.CenterPane;
import com.maxabrashov.imageshop.elements.ZoomScroll;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // Создаем сцену и Pane, который будет размещать 3 главных элемента GUI - InputImage, CenterPane, OutputImage
        BorderPane commonPane = new BorderPane();
        Scene scene = new Scene(commonPane,1200, 400);

        // Поделили GUI пополам
        ZoomScroll inputImage = new ZoomScroll(null, "Входное изображение"); // Левая часть
        ZoomScroll outputImage = new ZoomScroll(null, "Выходное изображение"); // Центр
        CenterPane centerPaneElements = new CenterPane(stage, inputImage, outputImage, scene); // Правая часть

        // Мини-адаптивизация под разные экраны ( расширяется экран - расширяются и ZoomScroll )
        stage.widthProperty().addListener((observableValue, oldVal, newVal) -> {
            if (stage.getWidth() >= 1200 && oldVal.intValue() != 0) {
                inputImage.setMinWidth(inputImage.getMinWidth() - ((double) (oldVal.intValue() - newVal.intValue()) /2));
                outputImage.setMinWidth(outputImage.getMinWidth() - ((double) (oldVal.intValue() - newVal.intValue()) /2));
            }
        });

        // Засовываем в основную часть блоки
        commonPane.setLeft(inputImage); // [*..]
        commonPane.setCenter(centerPaneElements); // [.*.]
        commonPane.setRight(outputImage); // [..*]

        // Устанавливаем стиль и название
        scene.getStylesheets().add("style.css");
        stage.setTitle("ImageShop - Русский аналог фотошопа!");

        // Устанавливаем минимальные значения для экрана
        stage.setMinWidth(1200);
        stage.setMinHeight(400);

        // Устанавливаем сцену и показываем её
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally(); // Нужно для загрузки OpenCV
        launch();
    }
}