package application;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    private double lastX, lastY;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc, canvas);

        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        Slider sizeSlider = new Slider(1, 30, 3);
        Button clearBtn = new Button("Очистить");

        clearBtn.setOnAction(e -> clearCanvas(gc, canvas));

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            lastX = e.getX();
            lastY = e.getY();
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            gc.beginPath();
            gc.moveTo(lastX, lastY);
            gc.stroke();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            double x = e.getX();
            double y = e.getY();
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            gc.lineTo(x, y);
            gc.stroke();
            lastX = x;
            lastY = y;
        });

        HBox tools = new HBox(8, colorPicker, sizeSlider, clearBtn);
        BorderPane root = new BorderPane();
        root.setTop(tools);
        root.setCenter(canvas);

        Scene scene = new Scene(root);
        stage.setTitle("Простое рисование");
        stage.setScene(scene);
        stage.show();
    }

    private void clearCanvas(GraphicsContext gc, Canvas canvas) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public static void main(String[] args) {
        launch();
    }
}
