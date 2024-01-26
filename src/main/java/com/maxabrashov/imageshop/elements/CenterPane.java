package com.maxabrashov.imageshop.elements;

import com.maxabrashov.imageshop.utils.ThreadHandler;
import com.maxabrashov.imageshop.utils.Utils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.maxabrashov.imageshop.utils.Utils.*;

public class CenterPane extends BorderPane {
    private Mat openFile;
    private ThreadHandler thread;
    public CenterPane(Stage stage, ZoomScroll inputImage, ZoomScroll outputImage, Scene scene) {
        // label - Обычный текст, который находится внутри центрального Pane
        Label label = createCommonLabels("ПАНЕЛЬ УПРАВЛЕНИЯ", "HeadLabel");


        // centerPane - содержит в себе все Slider's и Button's который отвечают за редактирование фото ( или открытия / закрытия )
        VBox centerPane = createVBox();

        // Слайдеры, корректирующие яркость и контраст
        Slider sliderLight = createCommonSlider("Яркость", centerPane, Pos.CENTER, -100, 100);
        Slider sliderKontrast = createCommonSlider("Контраст", centerPane, Pos.CENTER, 1, 10);

        // Вертикальный Pane, для слайдеров пороговой сегментации
        VBox inRangePane = createVBox();

        Label nameInRange = createCommonLabels("Пороговая сегментация", null);
        CheckBox isSegment = new CheckBox("Учитывать пороговую сегментацию?");
        inRangePane.getChildren().addAll(nameInRange, isSegment);
        // Слайдеры пороговой сегментации
        TextField fieldR1 = new TextField("0");
        Slider sliderR1 = createCommonSlider("[МИН] Красный", fieldR1, inRangePane, Pos.CENTER, 0, 255);
        TextField fieldG1 = new TextField("0");
        Slider sliderG1 = createCommonSlider("[МИН] Зеленый", fieldG1, inRangePane, Pos.CENTER, 0, 255);
        TextField fieldB1 = new TextField("0");
        Slider sliderB1 = createCommonSlider("[МИН] Синий", fieldB1, inRangePane, Pos.CENTER, 0, 255);
        TextField fieldR2 = new TextField("0");
        Slider sliderR2 = createCommonSlider("[МАКС] Красный", fieldR2, inRangePane, Pos.CENTER, 0, 255);
        TextField fieldG2 = new TextField("0");
        Slider sliderG2 = createCommonSlider("[МАКС] Зеленый", fieldG2, inRangePane, Pos.CENTER, 0, 255);
        TextField fieldB2 = new TextField("0");
        Slider sliderB2 = createCommonSlider("[МАКС] Синий", fieldB2, inRangePane, Pos.CENTER, 0, 255);

        // Загружаем поток
        HashMap<String, Slider> sliderHashMap = new HashMap<>() {{
            put("Light", sliderLight);
            put("Kontrast", sliderKontrast);
            put("R1", sliderR1);
            put("G1", sliderG1);
            put("B1", sliderB1);
            put("R2", sliderR2);
            put("G2", sliderG2);
            put("B2", sliderB2);
        }};
        thread = new ThreadHandler(outputImage, openFile, sliderHashMap, isSegment.isSelected());

        // ============================================== КНОПКИ ==============================================
        // Кнопка открытия
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg", "*.jpe", "*.jp2", "*.bmp");
        Button buttonOpen = new Button("Открыть");
        buttonOpen.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setTitle("Выберите изображение");
            while (true) { // Это проверка, если пользователь смог выбрать другой формат файла
                File selectedFile = fileChooser.showOpenDialog(stage);
                if (selectedFile == null) { break; }
                String[] splitNameFile = selectedFile.getName().split("\\.");
                if (splitNameFile[splitNameFile.length-1].equals("jpg") ||
                        splitNameFile[splitNameFile.length-1].equals("png") ||
                        splitNameFile[splitNameFile.length-1].equals("jpeg") ||
                        splitNameFile[splitNameFile.length-1].equals("jpe") ||
                        splitNameFile[splitNameFile.length-1].equals("bmp") ||
                        splitNameFile[splitNameFile.length-1].equals("jp2")) {
                    try {
                        BufferedImage in = ImageIO.read(selectedFile);
                        openFile = Utils.BufferedImage2Mat(in);
                        sliderLight.setValue(0);
                        sliderKontrast.setValue(0);

                        inputImage.setImage(Utils.Mat2Image(openFile));
                        break;
                    }
                    catch (IOException e) { throw new RuntimeException(e); }
                }
            }
        });

        // Кнопка закрытия
        Button buttonClose = new Button("Закрыть");
        buttonClose.setOnAction(actionEvent -> {
            openFile = null;
            inputImage.setText();
            outputImage.setText();
            if (thread != null) { thread.interrupt(); } // Выключаем поток
        });

        // Кнопка обработки
        Button buttonStart = new Button("Обработать");
        buttonStart.setOnAction(actionEvent -> {
            if (openFile == null) { // Проверяем, открыт ли у нас файл
                Alert alert = createAlert("Ошибка!", "Чтобы обработать фотографию - нужно открыть эту фотографию", Alert.AlertType.WARNING);
                alert.showAndWait();
                return;
            }
            if (thread != null && thread.isAlive()) { // Если идет второй поток, спрашиваем, закрыть ли его нам?
                Alert alert = createAlert("Обнаружен поток!", "В данный момент уже идет обработка. Вы хотите её прервать?", Alert.AlertType.CONFIRMATION);
                Optional<ButtonType> result = alert.showAndWait();
                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                    thread.interrupt();
                    thread.updateData(openFile, isSegment.isSelected());
                    thread.start();
                }
            }
            else {
                if (thread != null) {
                    thread.updateData(openFile, isSegment.isSelected());
                    thread.run();
                }
                else { thread = new ThreadHandler(outputImage, openFile, sliderHashMap, isSegment.isSelected()); }
            }
        });
        // ====================================================================================================

        // ========================================== ГОРЯЧИЕ КЛАВИШИ =========================================
        // E - Обработать
        // ESC - Закрыть
        // TAB - Открыть
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.E) {
                buttonStart.fire();
            }
            else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                buttonClose.fire();
            }
            else if (keyEvent.getCode() == KeyCode.TAB) {
                buttonOpen.fire();
            }
        });
        // ====================================================================================================

        centerPane.setSpacing(5);
        centerPane.getChildren().addAll(inRangePane, buttonOpen, buttonClose, buttonStart);

        ScrollPane scrollPaneCenter = new ScrollPane();
        scrollPaneCenter.setContent(centerPane);
        scrollPaneCenter.setMaxWidth(Double.MAX_VALUE);
        scrollPaneCenter.setFitToHeight(true);
        scrollPaneCenter.setFitToWidth(true);
        this.setCenter(scrollPaneCenter);
        this.setTop(label);
    }


}
