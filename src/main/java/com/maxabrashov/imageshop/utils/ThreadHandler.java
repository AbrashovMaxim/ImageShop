package com.maxabrashov.imageshop.utils;

import com.maxabrashov.imageshop.elements.ZoomScroll;
import javafx.application.Platform;
import javafx.scene.control.Slider;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;

import static com.maxabrashov.imageshop.utils.Utils.Mat2Image;
import static org.opencv.core.Core.bitwise_and;
import static org.opencv.core.Core.bitwise_not;

public class ThreadHandler extends Thread {
    private ZoomScroll outputImage;
    private Mat openFile;
    private HashMap<String, Slider> sliderHashMap;
    private boolean isSegment;
    public ThreadHandler(ZoomScroll outputImage, Mat openFile, HashMap<String, Slider> sliderHashMap, boolean isSegment) {
        super();

        this.outputImage = outputImage;
        this.openFile = openFile;
        this.sliderHashMap = sliderHashMap;
        this.isSegment = isSegment;
    }

    public void updateData(Mat openFile, boolean isSegment) {
        this.openFile = openFile;
        this.isSegment = isSegment;
    }

    @Override
    public void run() {
        // Яркость
        Mat imgHSV = new Mat();
        Imgproc.cvtColor(openFile, imgHSV, Imgproc.COLOR_BGR2HSV);
        Core.add(imgHSV, new Scalar(0, 0, sliderHashMap.get("Light").getValue()), imgHSV);
        Mat imgBGR = new Mat();
        Imgproc.cvtColor(imgHSV, imgBGR, Imgproc.COLOR_HSV2BGR);

        // Контраст
        Mat dest = new Mat(imgBGR.rows(), imgBGR.cols(), imgBGR.type());
        imgBGR.convertTo(dest, -1, sliderHashMap.get("Kontrast").getValue(), 0);

        // Сегментация
        if (isSegment) {
            Mat hsv = new Mat();
            Imgproc.cvtColor(dest, hsv, Imgproc.COLOR_BGR2HSV);
            Mat h = new Mat();
            Core.extractChannel(hsv, h, 0);
            Mat result = new Mat();
            Core.inRange(hsv,
                    new Scalar(sliderHashMap.get("R1").getValue(), sliderHashMap.get("G1").getValue(), sliderHashMap.get("B1").getValue()),
                    new Scalar(sliderHashMap.get("R2").getValue(), sliderHashMap.get("G2").getValue(), sliderHashMap.get("B2").getValue()),
                    result);
            bitwise_not(dest, dest, result);
        }
        Platform.runLater(() -> outputImage.setImage(Mat2Image(dest)));
    }
}
