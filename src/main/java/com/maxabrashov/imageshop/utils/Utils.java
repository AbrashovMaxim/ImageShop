package com.maxabrashov.imageshop.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {
    // Конвертер из MAT ( OpenCV ) в Image ( JavaFX )
    public static Image Mat2Image(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    // Конвертер из BufferedImage ( Swing ) в MAT ( OpenCV )
    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    }

    // Создание обычных Label, который ставятся по центру блока
    public static Label createCommonLabels(String text, String id) {
        Label label = new Label(text);
        label.setId(id);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    // Создание Slider с TextField + Label
    public static Slider createCommonSlider(String name, TextField element, Pane parent, Pos position, int min, int max) {
        Slider slider = new Slider();

        // При изменение значений в слайдере - изменяются и значения в TextField
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldVal, Number newVal) {
                element.setText(String.valueOf(newVal.intValue()));
            }
        });
        slider.setMax(max);
        slider.setMin(min);
        slider.setValue(0);
        slider.setMinWidth(250);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        Label label_1 = new Label(name, element);
        element.setMaxWidth(100);

        // Бокс используется, для того, чтобы сделать TextField + Label и под ними Slider
        VBox box = new VBox();
        box.getChildren().add(label_1);
        box.getChildren().add(slider);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setAlignment(position);

        parent.getChildren().add(box);

        // ====================================== ФИЛЬТЕР ЧИСЕЛ В ЯЧЕЙКАХ ===============================================
        element.textProperty().addListener((observableValue, oldValue, newVal) -> {
            if (newVal.equals("0")) { return; }
            for(int i = 0; i < newVal.length(); i++) {
                if(!Character.isDigit(newVal.charAt(i))) {
                    element.setText(oldValue);
                    slider.setValue(Double.parseDouble(oldValue));
                    return;
                }
                if (newVal.charAt(0) == '0' && newVal.length() > 1) {
                    String _t = newVal.substring(1); // Временная переменная
                    element.setText(_t);
                    slider.setValue(Double.parseDouble(_t));
                    return;
                }
            }
            if (newVal.length() > 1 ) {
                if (Integer.parseInt(newVal) > max) {
                    element.setText(String.valueOf(max));
                    slider.setValue(max);
                }
                else if (Integer.parseInt(newVal) < min) {
                    element.setText(String.valueOf(min));
                    slider.setValue(min);

                }
            }
        });
        // ==============================================================================================================
        // ====================== С ПОМОЩЬЮ СТРЕЛОЧЕК UP и DOWN УВЕЛИЧЕНИЕ ЧИСЛА В ЯЧЕЙКЕ ===============================
        element.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.UP) {
                    if (slider.getValue() < slider.getMax()) slider.setValue(slider.getValue()+1);
                }
                else if(keyEvent.getCode() == KeyCode.DOWN) {
                    if (slider.getValue() > slider.getMin()) slider.setValue(slider.getValue()-1);
                }
            }
        });
        // ==============================================================================================================
        return slider;
    }

    // Создание Slider + Label
    public static Slider createCommonSlider(String name, Pane parent, Pos position, int min, int max) {
        Slider slider = new Slider();
        slider.setMax(max);
        slider.setMin(min);
        slider.setValue(0);
        slider.setMinWidth(250);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);

        Label label = new Label(name, slider);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(position);
        label.setContentDisplay(ContentDisplay.BOTTOM);

        parent.getChildren().add(label);

        return slider;
    }

    // Создание Диалогового окна
    public static Alert createAlert(String name, String contentText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(name);
        alert.setContentText(contentText);
        return alert;
    }

    // Создание Вертикального бокса, у которого ширина, как у родителя, и блоки внутри стоят по центру
    public static VBox createVBox() {
        VBox vBox = new VBox();
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(5);
        return vBox;
    }

}
