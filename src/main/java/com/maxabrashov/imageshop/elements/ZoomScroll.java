package com.maxabrashov.imageshop.elements;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ZoomScroll extends ScrollPane {

    private double scaleValue = 0.7; // Сила увеличения
    private double zoomIntensity = 0.02; // Интенсивность
    private Node target; // Элемент который увеличивается
    private Node zoomNode; // Объект приближения
    private String nameText; // Текст, который будет писаться вместо изображения
    public ZoomScroll(Node element, String nameText) {
        super();

        setPannable(true);
        setFitToHeight(true);
        setFitToWidth(true);
        setMinWidth(400);
        setMaxWidth(400);

        this.nameText = nameText;

        if (element == null) { // Если у нас не передается какой-то элемент, то в зум вставляем текст
            element = new Text(nameText);
            setHbarPolicy(ScrollBarPolicy.NEVER);
            setVbarPolicy(ScrollBarPolicy.NEVER);
        }
        this.target = element;
        this.zoomNode = new Group(target);
        setContent(outNode(this.zoomNode)); // Устанавливаем контент
        updateScale();
    }

    private Node outNode(Node node) {
        Node outerNode = centeredNode(node);
        outerNode.setOnScroll(e -> { // Выдаем элементу слушатель
            if (!(this.target.getClass() == ImageView.class)) { // Если в ZoomScroll находится просто текст, то им нельзя пользоваться
                e.consume();
                return;
            }

            if (e.isControlDown()) { // Если зажат CTRL, то можно приблежать
                e.consume();
                onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
            }
        });
        return outerNode;
    }

    private Node centeredNode(Node node) { // Центрируем элемент
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private void updateScale() { // Обновляем масштаб элемента
        this.target.setScaleX(this.scaleValue);
        this.target.setScaleY(this.scaleValue);
    }

    private void onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity); // Фактор масштабирования, в зависимости от силы прокрутки колесика мыши

        Bounds innerBounds = zoomNode.getLayoutBounds(); // Получаем границы элемента
        Bounds viewportBounds = getViewportBounds(); // Получаем границы видимой области

        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth()); // Получаем горизонтальное значение прокрутки
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight()); // Получаем вертикальное значение прокрутки

        scaleValue = scaleValue * zoomFactor; // Умножаем текущее значение, на фактор масштабирования
        updateScale(); // Обновляем масштаб элемента
        this.layout();

        Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint)); // Получаем позицию указателя мыши относительно элемента, который увеличивали
        Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1)); // Корректировки позиции указателя, после масштабирования

        Bounds updatedInnerBounds = zoomNode.getBoundsInLocal(); // Получаем ОБНОВЛЕННЫЕ границы после масштабирования
        this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth())); // Устанавливаем горизонтальное значение прокрутки
        this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight())); // Устанавливаем вертикальное значение прокрутки
    }

    public void setImage(Image image) { // Устанавливаем изображение
        this.target = new ImageView(image);
        this.zoomNode = new Group(target);
        setContent(outNode(this.zoomNode));
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        updateScale();
    }

    public void setText() { // Устанавливаем текст
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        this.target = new Text(this.nameText);
        this.zoomNode = new Group(target);
        setContent(outNode(this.zoomNode));
        updateScale();
    }
}
