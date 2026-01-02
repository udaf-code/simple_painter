package application;
	
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import application.Main.Tool;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
//import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Main extends Application {
    private double lastX, lastY;
    
    enum Tool { SIMPLE, LINE, RECT, ERASER }
    private Tool currentTool = Tool.SIMPLE;
    private double startX, startY;
    private Canvas canvas;
    private GraphicsContext gc;
    

    private final Deque<WritableImage> undoStack = new ArrayDeque<>();
    private final Deque<WritableImage> redoStack = new ArrayDeque<>();

    private WritableImage tempSnapshot = null;
    private final double ERASER_SIZE = 16;
    
    private ColorPicker colorPicker = new ColorPicker(Color.BLACK);
    private Slider sizeSlider = new Slider(1, 30, 3);

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        clearCanvas(gc, canvas);

        colorPicker = new ColorPicker(Color.BLACK);
        sizeSlider = new Slider(1, 30, 3);
        Button clearBtn = new Button("Очистить");

        clearBtn.setOnAction(e -> clearCanvas(gc, canvas));

//        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
//            lastX = e.getX();
//            lastY = e.getY();
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            gc.beginPath();
//            gc.moveTo(lastX, lastY);
//            gc.stroke();
//        });
//
//        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
//            double x = e.getX();
//            double y = e.getY();
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            gc.lineTo(x, y);
//            gc.stroke();
//            lastX = x;
//            lastY = y;
//        });
        VBox vbox = new VBox(8);

        HBox tools = new HBox(8, colorPicker, sizeSlider, clearBtn);
        BorderPane root = new BorderPane();
        
        
        ToggleButton lineBtn = new ToggleButton("Линия");
        ToggleButton rectBtn = new ToggleButton("Прямоуг");
        ToggleButton eraserBtn = new ToggleButton("Ластик");
        ToggleGroup tg = new ToggleGroup();
        lineBtn.setToggleGroup(tg);
        rectBtn.setToggleGroup(tg);
        eraserBtn.setToggleGroup(tg);
        lineBtn.setSelected(true);

        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        //Button clearBtn = new Button("Clear");
        Button saveBtn = new Button("Save");
        Button loadBtn = new Button("Load");

        HBox tools_two = new HBox(8, lineBtn, rectBtn, eraserBtn, undoBtn, redoBtn, saveBtn, loadBtn);
        tools.setStyle("-fx-padding: 8; -fx-background-color: #eee;");
        
        vbox.getChildren().addAll(tools, tools_two);
        root.setTop(vbox);
        root.setCenter(canvas);


        Scene scene = new Scene(root);
        stage.setTitle("Простое рисование");
        stage.setScene(scene);
        stage.show();
        
        colorPicker.setOnAction(e -> {
        	currentTool = Tool.SIMPLE;
        	});
        sizeSlider.setOnMouseReleased(e -> {
        	currentTool = Tool.SIMPLE;
        	});


        lineBtn.setOnAction(e -> currentTool = Tool.LINE);
        rectBtn.setOnAction(e -> currentTool = Tool.RECT);
        eraserBtn.setOnAction(e -> currentTool = Tool.ERASER);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);

        undoBtn.setOnAction(e -> undo());
        redoBtn.setOnAction(e -> redo());
        clearBtn.setOnAction(e -> {
            pushUndo();
            clearCanvas();
            redoStack.clear();
        });

        saveBtn.setOnAction(e -> saveToFile(stage));
        loadBtn.setOnAction(e -> loadFromFile(stage));
        pushUndo();
    }
    
    private void onMousePressed(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        startX = e.getX();
        startY = e.getY();
        //!!! потом переделать убрать лишнее привести логику к единому типу
        lastX = e.getX();
        lastY = e.getY();
        //
        //pushUndo();
        System.out.println("мышь");
        // простое рисование
        if (currentTool == Tool.SIMPLE) {
        	gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            gc.beginPath();
            gc.moveTo(lastX, lastY);
            gc.stroke();
        }
        else if (currentTool == Tool.RECT || currentTool == Tool.LINE) {
            tempSnapshot = canvas.snapshot(null, null);
        } else if (currentTool == Tool.ERASER) {
            pushUndo();
            redoStack.clear();
            eraseAt(startX, startY);
        }
    }

    private void onMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        if (currentTool == Tool.SIMPLE) {
        	gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            gc.lineTo(x, y);
            gc.stroke();
            lastX = x;
            lastY = y;
        }
        else if (currentTool == Tool.ERASER) {
            eraseAt(x, y);
        } else if (currentTool == Tool.LINE) {
            restoreSnapshot(tempSnapshot);
            // толщина линии и цвет из кнопок
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            //gc.setStroke(Color.BLACK);
            //gc.setLineWidth(2);
            gc.strokeLine(startX, startY, x, y);
        } else if (currentTool == Tool.RECT) {
            restoreSnapshot(tempSnapshot);
            // толщина линии и цвет из кнопок
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            //gc.setStroke(Color.BLACK);
            //gc.setLineWidth(2);
            double rx = Math.min(startX, x);
            double ry = Math.min(startY, y);
            double rw = Math.abs(x - startX);
            double rh = Math.abs(y - startY);
            gc.strokeRect(rx, ry, rw, rh);
        }
    }

    private void onMouseReleased(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        double x = e.getX();
        double y = e.getY();

        if (currentTool == Tool.SIMPLE) {
        	pushUndo();
            redoStack.clear();
            tempSnapshot = null;
        }
        else if (currentTool == Tool.LINE) {
            restoreSnapshot(tempSnapshot);
            // толщина линии и цвет из кнопок
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            //gc.setStroke(Color.BLACK);
            //gc.setLineWidth(2);
            gc.strokeLine(startX, startY, x, y);
            pushUndo();
            redoStack.clear();
            tempSnapshot = null;
        } else if (currentTool == Tool.RECT) {
            restoreSnapshot(tempSnapshot);
            // толщина линии и цвет из кнопок
            gc.setStroke(colorPicker.getValue());
            gc.setLineWidth(sizeSlider.getValue());
            //gc.setStroke(Color.BLACK);
            //gc.setLineWidth(2);
            double rx = Math.min(startX, x);
            double ry = Math.min(startY, y);
            double rw = Math.abs(x - startX);
            double rh = Math.abs(y - startY);
            gc.strokeRect(rx, ry, rw, rh);
            pushUndo();
            redoStack.clear();
            tempSnapshot = null;
        } else if (currentTool == Tool.ERASER) {
            // уже сделали pushUndo() при press
        }
    }
    
    private void undo() {
        if (undoStack.isEmpty()) return;
        WritableImage current = canvas.snapshot(null, null);
        redoStack.push(current);

        WritableImage prev = undoStack.pop();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(prev, 0, 0);
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        WritableImage current = canvas.snapshot(null, null);
        undoStack.push(current);

        WritableImage next = redoStack.pop();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(next, 0, 0);
    }

    private void clearCanvas(GraphicsContext gc, Canvas canvas) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    private void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
    }
    private void pushUndo() {
        WritableImage snap = canvas.snapshot(null, null);
        undoStack.push(snap);
        System.out.println(undoStack);
        // ограничение размера стека (опционально)
        if (undoStack.size() > 50) {
            // простая обрезка: удаляем самое старое (в данном простом примере не реализовано удаление нижнего элемента)
        }
    }
    private void eraseAt(double x, double y) {
        gc.clearRect(x - ERASER_SIZE / 2, y - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
    }
    
    private void restoreSnapshot(WritableImage snapshot) {
        if (snapshot != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(snapshot, 0, 0);
        }
    }
 // Сохранение в PNG
    private void saveToFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        FileChooser.ExtensionFilter ext = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(ext);
        fileChooser.setInitialFileName("drawing.png");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            WritableImage image = canvas.snapshot(null, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Загрузка PNG/JPG (подгоняем под размер холста)
    private void loadFromFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
                // сохранить текущее состояние для undo
                pushUndo();
                redoStack.clear();
                // очистить и нарисовать загруженное изображение, масштабируем под холст
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.drawImage(img, 0, 0, canvas.getWidth(), canvas.getHeight());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
