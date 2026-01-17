package application;
	
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import application.Main.Tool;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
//import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class Main extends Application {
    //private double lastX, lastY;
    
    enum Tool { CURVE, LINE, RECT, ROT_RECT, CIRCLE, STAR, ERASER }
    private Tool currentTool = Tool.CURVE;
    private double startX, startY;
    private Canvas canvas;
    private GraphicsContext gc;
    private final List<Drawable> model = new ArrayList<>();
    

    //private final Deque<WritableImage> undoStack = new ArrayDeque<>();
    //private final Deque<WritableImage> redoStack = new ArrayDeque<>();

    private WritableImage tempSnapshot = null;
    private final double ERASER_SIZE = 16;
    
    private ColorPicker colorPicker = new ColorPicker(Color.BLACK);
    private Slider sizeSlider = new Slider(1, 30, 3);
    
    private final UndoManager undoManager = new UndoManager(100);
    
    private UndoManagerSnap unManeger;
    
    private Stroke currentStroke = null;
    private Line currentLine = null;
    private Erase currentErase = null;
    private Circle currentCircle = null;
    private Rect currentRect = null;
    private Star currentStar = null;
    private RotRect currentRotRect = null;
    
    private boolean CommandUndoManager = false;

    /**
     *
     */
    @Override
    public void start(Stage stage) {
    	unManeger = new UndoManagerSnap();
    	// проверяем файл data.json
    	File file = new File("data.json");
    	if (!file.exists()){
    		// создаем
    		ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = Map.of(
                "type", "snapshot"
            );

            File out = new File("data.json");
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(out, data);
                System.out.println("Записано в " + out.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}
    	else {
    		// читаем
    		File in = new File("data.json");
    		
    		try {
    			ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data2 = mapper.readValue(in, new TypeReference<Map<String, Object>>() {});
                String name = (String) data2.get("type");
                System.out.println(name);
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        clearCanvas(gc, canvas);
        
        Menu fileMenu = new Menu("Файл");
        MenuItem saveItem = new MenuItem("Сохранить");
        MenuItem loadItem = new MenuItem("Загрузить");
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().addAll(saveItem, loadItem, new SeparatorMenuItem(), exitItem);
        
        Menu settingsMenu = new Menu("Настройки");
        MenuItem paramItem = new MenuItem("Параметры");
        settingsMenu.getItems().addAll(paramItem);

        // MenuBar
        MenuBar menuBar = new MenuBar(fileMenu, settingsMenu);
        paramItem.setOnAction(e -> paramWindow());


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
        
        ToggleButton curveBtn = new ToggleButton("Кривая");
        ToggleButton lineBtn = new ToggleButton("Линия");
        ToggleButton rectBtn = new ToggleButton("Прямоуг");
        ToggleButton rotRectBtn = new ToggleButton("Прямоуг с пов");
        // круг
        ToggleButton circleBtn = new ToggleButton("Круг");
        ToggleButton starBtn = new ToggleButton("Звезда");
        ToggleButton eraserBtn = new ToggleButton("Ластик");
        ToggleGroup tg = new ToggleGroup();
        curveBtn.setToggleGroup(tg);
        lineBtn.setToggleGroup(tg);
        circleBtn.setToggleGroup(tg);
        starBtn.setToggleGroup(tg);
        rectBtn.setToggleGroup(tg);
        rotRectBtn.setToggleGroup(tg);
        eraserBtn.setToggleGroup(tg);
        curveBtn.setSelected(true);

        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        //Button clearBtn = new Button("Clear");
        Button saveBtn = new Button("Save");
        Button loadBtn = new Button("Load");

        HBox tools_two = new HBox(8, curveBtn, lineBtn, circleBtn, starBtn, rectBtn, rotRectBtn, eraserBtn, undoBtn, redoBtn, saveBtn, loadBtn);
        tools.setStyle("-fx-padding: 8; -fx-background-color: #eee;");
        
        vbox.getChildren().addAll(menuBar, tools, tools_two);
        root.setTop(vbox);
        root.setCenter(canvas);


        Scene scene = new Scene(root);
        stage.setTitle("Простое рисование");
        stage.setScene(scene);
        stage.show();
        
//        colorPicker.setOnAction(e -> {
//        	currentTool = Tool.SIMPLE;
//        	});
//        sizeSlider.setOnMouseReleased(e -> {
//        	currentTool = Tool.SIMPLE;
//        	});

        curveBtn.setOnAction(e -> currentTool = Tool.CURVE);
        lineBtn.setOnAction(e -> currentTool = Tool.LINE);
        rectBtn.setOnAction(e -> currentTool = Tool.RECT);
        rotRectBtn.setOnAction(e -> currentTool = Tool.ROT_RECT);
        circleBtn.setOnAction(e -> currentTool = Tool.CIRCLE);
        starBtn.setOnAction(e -> currentTool = Tool.STAR);
        eraserBtn.setOnAction(e -> currentTool = Tool.ERASER);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);

//        undoBtn.setOnAction(e -> undo());
//        redoBtn.setOnAction(e -> redo());
        
        
        if (CommandUndoManager)  {
	        undoBtn.setOnAction(e -> {
	            undoManager.undo();
	            redraw();
	            //updateButtons(undoBtn, redoBtn);
	        });
	        redoBtn.setOnAction(e -> {
	            undoManager.redo();
	            redraw();
	            //updateButtons(undoBtn, redoBtn);
	        });
        }
        else {
        	undoBtn.setOnAction(e -> unManeger.undo(canvas,gc));
        	redoBtn.setOnAction(e -> unManeger.redo(canvas,gc));
        	
        }
        clearBtn.setOnAction(e -> {
        	unManeger.pushUndo(canvas);
            //pushUndo();
            clearCanvas();
            // исправить все редостеки
            unManeger.clearRedoStack();
            //redoStack.clear();
        });

        saveBtn.setOnAction(e -> saveToFile(stage));
        loadBtn.setOnAction(e -> loadFromFile(stage));
        unManeger.pushUndo(canvas);
        //pushUndo();
        // selection manager
        SelectionManager sel = new SelectionManager();
        // регистрируем контролы
        sel.register(curveBtn);
        sel.register(lineBtn);
        sel.register(rectBtn);
        sel.register(rotRectBtn);
        sel.register(circleBtn);
        sel.register(eraserBtn);
        sel.register(undoBtn);
        sel.register(redoBtn);
        sel.register(colorPicker);
        sel.register(sizeSlider);
    }
    // окно настроек
    public void paramWindow() {
    	String name = "";
    	Stage window = new Stage();
    	// прикрутить gson если нет gson то опция 1 либол 2
    	ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Shapshots", "Command");
        // добавить логику выбора от чтения json
        // читаем
		File in = new File("data.json");
		
		try {
			ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data2 = mapper.readValue(in, new TypeReference<Map<String, Object>>() {});
            name = (String) data2.get("type");
            System.out.println(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        choiceBox.setValue(name); // 
        // choisebox обработа событий
        choiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) 
            {
                // записать newVal в файл (в отдельном потоке!)
            	
            	System.out.println(newVal);
            	// создаем
        		ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data = Map.of(
                    "type", newVal
                );

                File out = new File("data.json");
                try {
                    mapper.writerWithDefaultPrettyPrinter().writeValue(out, data);
                    System.out.println("Записано в " + out.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    	VBox pane = new VBox(10.0, choiceBox);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10.0));
        Scene scene = new Scene(pane, 400, 250);
        
        window.setScene(scene);
        window.setTitle("Параметры");
        window.show();
    }
    private void onMousePressed(MouseEvent e) {
    	if (e.getButton() != MouseButton.PRIMARY) return;
    	if (currentTool == Tool.CURVE) {
    		currentStroke = new Stroke(colorPicker.getValue(), sizeSlider.getValue());
            currentStroke.addPoint(e.getX(), e.getY());
    	}
    	else if (currentTool == Tool.LINE) {
    		currentLine = new Line(colorPicker.getValue(), sizeSlider.getValue());
    		currentLine.addPoint(e.getX(), e.getY());
    		
    		tempSnapshot = canvas.snapshot(null, null);
    	}
    	else if (currentTool == Tool.ERASER) {
    		currentErase = new Erase();
    		currentErase.addPoint(e.getX(), e.getY());
    		//eraseAt(e.getX(), e.getY());
    	}
    	else if (currentTool == Tool.CIRCLE) {
    		currentCircle = new Circle(colorPicker.getValue(), sizeSlider.getValue());
    		currentCircle.addPoint(e.getX(), e.getY());
    	}
    	else if (currentTool == Tool.RECT) {
    		currentRect = new Rect(colorPicker.getValue(), sizeSlider.getValue());
    		currentRect.addPoint(e.getX(), e.getY());
    	}
    	else if (currentTool == Tool.STAR) {
    		currentStar = new Star(colorPicker.getValue(), sizeSlider.getValue());
    		currentStar.addPoint(e.getX(), e.getY());
    	}
    	else if (currentTool == Tool.ROT_RECT) {
    		currentRotRect = new RotRect(colorPicker.getValue(), sizeSlider.getValue());
    		currentRotRect.addPoint(e.getX(), e.getY());
    	}
    }
    private void onMouseDragged(MouseEvent e) {
    	if (e.getButton() != MouseButton.PRIMARY) return;
    	if (currentTool == Tool.CURVE) {
            if (currentStroke != null) {
                currentStroke.addPoint(e.getX(), e.getY());
                redraw(); // перерисовываем всё модель + текущий черновик
                drawStroke(currentStroke);
            }
    	}
		else if (currentTool == Tool.LINE) {
			if (currentLine != null) {
				currentLine.addPoint(e.getX(), e.getY());
				redraw();
				drawLine(currentLine);
			}
		}
		else if (currentTool == Tool.ERASER) {
			if (currentErase != null) {
				currentErase.addPoint(e.getX(), e.getY());
				redraw();
				drawErase(currentErase);
			//eraseAt(e.getX(), e.getY());  		
			}
		}
		else if (currentTool == Tool.CIRCLE) {
			if (currentCircle != null) {
				currentCircle.addPoint(e.getX(), e.getY());
				redraw();
				drawCircle(currentCircle);		
			}
		}
		else if (currentTool == Tool.RECT) {
			if (currentRect != null) {
				currentRect.addPoint(e.getX(), e.getY());
				redraw();
				drawRect(currentRect);		
			}    		
		}
		else if (currentTool == Tool.STAR) {
			if (currentStar != null) {
				currentStar.addPoint(e.getX(), e.getY());
				redraw();
				drawStar(currentStar); 	
			}
		}
		else if (currentTool == Tool.ROT_RECT) {
			if (currentRotRect != null) {
				currentRotRect.addPoint(e.getX(), e.getY());
				redraw();
				drawRotRect(currentRotRect); 	
			}
    	}
    }
    private void onMouseReleased(MouseEvent e) {
    	if (e.getButton() != MouseButton.PRIMARY) return;
    	if (currentTool == Tool.CURVE) {
            if (currentStroke != null) {
                currentStroke.addPoint(e.getX(), e.getY());
                // Создаём команду и выполняем её через UndoManager
                if (CommandUndoManager) {
	                DrawStrokeCommand cmd = new DrawStrokeCommand(model, currentStroke);
	                undoManager.doCommand(cmd);
                }
                currentStroke = null;
                redraw();
                //updateButtons(undoBtn, redoBtn);
            }
    	}
    	else if (currentTool == Tool.LINE) {
    		if (currentLine != null) {
				currentLine.addPoint(e.getX(), e.getY());
				// Создаём команду и выполняем её через UndoManager
				//if (CommandUndoManager) {
					DrawLineCommand cmd = new DrawLineCommand(model, currentLine);
	                undoManager.doCommand(cmd);
				//}
                currentLine = null;
				redraw();
				
				//restoreSnapshot(tempSnapshot);
				//updateButtons(undoBtn, redoBtn);
				//drawLine(currentLine);
				unManeger.pushUndo(canvas);
	            //pushUndo();
	            unManeger.clearRedoStack();
	            //redoStack.clear();
	            tempSnapshot = null;
			}
		}
    	else if (currentTool == Tool.ERASER) {
    		if (currentErase != null) {
    			if (CommandUndoManager) {
	    			DrawEraseCommand cmd = new DrawEraseCommand(model, currentErase);
	    			undoManager.doCommand(cmd);
    			}
    			currentErase = null;
    			redraw();
				//updateButtons(undoBtn, redoBtn);
    		}
    	}
    	else if (currentTool == Tool.CIRCLE) {
    		if (currentCircle != null) {
    			if (CommandUndoManager) {
	    			DrawCircleCommand cmd = new DrawCircleCommand(model, currentCircle);
	    			undoManager.doCommand(cmd);
    			}
    			currentCircle = null;
    			redraw();
				//updateButtons(undoBtn, redoBtn);
    		}
    	}
		else if (currentTool == Tool.RECT) {
			if (currentRect != null) {
				if (CommandUndoManager) {
	    			DrawRectCommand cmd = new DrawRectCommand(model, currentRect);
	    			undoManager.doCommand(cmd);
				}
    			currentRect = null;
    			redraw();
				//updateButtons(undoBtn, redoBtn);
    		}   		
		}
		else if (currentTool == Tool.STAR) {
			if (currentStar != null) {
				if (CommandUndoManager) {
	    			DrawStarCommand cmd = new DrawStarCommand(model, currentStar);
	    			undoManager.doCommand(cmd);
				}
    			currentStar = null;
    			redraw();
				//updateButtons(undoBtn, redoBtn);
    		}     		
		}
		else if (currentTool == Tool.ROT_RECT) {
			if (currentRotRect != null) {
				if (CommandUndoManager) {
	    			DrawRotRectCommand cmd = new DrawRotRectCommand(model, currentRotRect);
	    			undoManager.doCommand(cmd);
				}
    			currentRotRect = null;
    			redraw();
				//updateButtons(undoBtn, redoBtn);
    		} 
    	}
    }
    
    
//    private void onMousePressed(MouseEvent e) {
//        if (e.getButton() != MouseButton.PRIMARY) return;
//        startX = e.getX();
//        startY = e.getY();
//        //!!! потом переделать убрать лишнее привести логику к единому типу
////        lastX = e.getX();
////        lastY = e.getY();
//        //
//        //pushUndo();
//        System.out.println("мышь");
//        // простое рисование
//        if (currentTool == Tool.CURVE) {
//        	gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            gc.beginPath();
//            gc.moveTo(startX, startY);
//            //gc.moveTo(lastX, lastY);
//            gc.stroke();
//        }
//        else if (currentTool == Tool.RECT || currentTool == Tool.LINE|| currentTool == Tool.CIRCLE|| currentTool == Tool.STAR|| currentTool == Tool.ROT_RECT) {
//            tempSnapshot = canvas.snapshot(null, null);
//        } else if (currentTool == Tool.ERASER) {
//        	unManeger.pushUndo(canvas);
//            //pushUndo();
//        	unManeger.clearRedoStack();
//            //redoStack.clear();
//            eraseAt(startX, startY);
//        }
//    }

//    private void onMouseDragged(MouseEvent e) {
//        double x = e.getX();
//        double y = e.getY();
//
//        if (currentTool == Tool.CURVE) {
//        	gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            gc.lineTo(x, y);
//            gc.stroke();
////            lastX = x;
////            lastY = y;
//            startX = x;
//            startY = y;
//        }
//        else if (currentTool == Tool.ERASER) {
//            eraseAt(x, y);
//        } else if (currentTool == Tool.LINE) {
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            //gc.setStroke(Color.BLACK);
//            //gc.setLineWidth(2);
//            gc.strokeLine(startX, startY, x, y);
//        
//        } else if (currentTool == Tool.RECT) {
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            //gc.setStroke(Color.BLACK);
//            //gc.setLineWidth(2);
//            double rx = Math.min(startX, x);
//            double ry = Math.min(startY, y);
//            double rw = Math.abs(x - startX);
//            double rh = Math.abs(y - startY);
//            gc.strokeRect(rx, ry, rw, rh);
//        }else if (currentTool == Tool.ROT_RECT) {
//        	drawPreviewRect(startX, startY, x, y);
//        }else if (currentTool == Tool.CIRCLE) {
//        
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            //gc.setStroke(Color.BLACK);
//            //gc.setLineWidth(2);
//            double rx = Math.min(startX, x);
//            double ry = Math.min(startY, y);
//            double rw = Math.abs(x - startX);
//            double rh = Math.abs(y - startY);
//            //gc.strokeRect(rx, ry, rw, rh);
//            gc.strokeOval(rx, ry, rw, rh);
//        } else if (currentTool == Tool.STAR) {
//        	// вынести в отдельный метод
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//         // startX, startY - точка начала (например первый клик), x, y - текущая позиция мыши
//            // используем ограничивающий прямоугольник как у прямоугольника
//            double rx = Math.min(startX, x);
//            double ry = Math.min(startY, y);
//            double rw = Math.abs(x - startX);
//            double rh = Math.abs(y - startY);
//
//            // центр и внешний радиус (по меньшей стороне прямоугольника)
//            double cx = rx + rw / 2.0;
//            double cy = ry + rh / 2.0;
//            double outerRadius = Math.min(rw, rh) / 2.0;
//            // внутренний радиус задаём как долю внешнего (0.4-0.5 обычно хорошо)
//            double innerRadius = outerRadius * 0.5;
//
//            // количество вершин: 5-конечная звезда -> 10 точек чередующихся
//            int points = 10;
//            double[] xs = new double[points];
//            double[] ys = new double[points];
//
//            // смещение угла так, чтобы один луч смотрел вверх (можно изменить)
//            double startAngle = -Math.PI / 2.0; // вверх
//            for (int i = 0; i < points; i++) {
//                double angle = startAngle + i * (2 * Math.PI / points);
//                double r = (i % 2 == 0) ? outerRadius : innerRadius;
//                xs[i] = cx + Math.cos(angle) * r;
//                ys[i] = cy + Math.sin(angle) * r;
//            }
//
//            // рисуем замкнутый контур
//            gc.strokePolygon(xs, ys, points);
//
//        }
//	}

//    private void onMouseReleased(MouseEvent e) {
//        if (e.getButton() != MouseButton.PRIMARY) return;
//        double x = e.getX();
//        double y = e.getY();
//
//        if (currentTool == Tool.CURVE) {
//        	unManeger.pushUndo(canvas);
//        	//pushUndo();
//        	unManeger.clearRedoStack();
//            //redoStack.clear();
//            tempSnapshot = null;
//        }
//        else if (currentTool == Tool.LINE) {
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            //gc.setStroke(Color.BLACK);
//            //gc.setLineWidth(2);
//            gc.strokeLine(startX, startY, x, y);
//            unManeger.pushUndo(canvas);
//            //pushUndo();
//            unManeger.clearRedoStack();
//            //redoStack.clear();
//            tempSnapshot = null;
//        } else if (currentTool == Tool.RECT) {
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            //gc.setStroke(Color.BLACK);
//            //gc.setLineWidth(2);
//            double rx = Math.min(startX, x);
//            double ry = Math.min(startY, y);
//            double rw = Math.abs(x - startX);
//            double rh = Math.abs(y - startY);
//            gc.strokeRect(rx, ry, rw, rh);
//            unManeger.pushUndo(canvas);
//            //pushUndo();
//            unManeger.clearRedoStack();
//            //redoStack.clear();
//            tempSnapshot = null;
//        }else if (currentTool == Tool.ROT_RECT) {
//        	drawPreviewRect(startX, startY, x, y);
//        	unManeger.pushUndo(canvas);
//            //pushUndo();
//        	unManeger.clearRedoStack();
//            //redoStack.clear();
//            tempSnapshot = null;
//        }else if (currentTool == Tool.CIRCLE) {
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//            //gc.setStroke(Color.BLACK);
//            //gc.setLineWidth(2);
//            double rx = Math.min(startX, x);
//            double ry = Math.min(startY, y);
//            double rw = Math.abs(x - startX);
//            double rh = Math.abs(y - startY);
//            gc.strokeOval(rx, ry, rw, rh);
//            unManeger.pushUndo(canvas);
//            //pushUndo();
//            unManeger.clearRedoStack();
//            //redoStack.clear();
//            tempSnapshot = null;
//        }else if (currentTool == Tool.STAR) {
//        	// вынести в отдельный метод
//            restoreSnapshot(tempSnapshot);
//            // толщина линии и цвет из кнопок
//            gc.setStroke(colorPicker.getValue());
//            gc.setLineWidth(sizeSlider.getValue());
//         // startX, startY - точка начала (например первый клик), x, y - текущая позиция мыши
//            // используем ограничивающий прямоугольник как у прямоугольника
//            double rx = Math.min(startX, x);
//            double ry = Math.min(startY, y);
//            double rw = Math.abs(x - startX);
//            double rh = Math.abs(y - startY);
//
//            // центр и внешний радиус (по меньшей стороне прямоугольника)
//            double cx = rx + rw / 2.0;
//            double cy = ry + rh / 2.0;
//            double outerRadius = Math.min(rw, rh) / 2.0;
//            // внутренний радиус задаём как долю внешнего (0.4-0.5 обычно хорошо)
//            double innerRadius = outerRadius * 0.5;
//
//            // количество вершин: 5-конечная звезда -> 10 точек чередующихся
//            int points = 10;
//            double[] xs = new double[points];
//            double[] ys = new double[points];
//
//            // смещение угла так, чтобы один луч смотрел вверх (можно изменить)
//            double startAngle = -Math.PI / 2.0; // вверх
//            for (int i = 0; i < points; i++) {
//                double angle = startAngle + i * (2 * Math.PI / points);
//                double r = (i % 2 == 0) ? outerRadius : innerRadius;
//                xs[i] = cx + Math.cos(angle) * r;
//                ys[i] = cy + Math.sin(angle) * r;
//            }
//
//            // рисуем замкнутый контур
//            gc.strokePolygon(xs, ys, points);
//            unManeger.pushUndo(canvas);
//            //pushUndo();
//            unManeger.clearRedoStack();
//            //redoStack.clear();
//            tempSnapshot = null;
//
//        }else if (currentTool == Tool.ERASER) {
//            // уже сделали pushUndo() при press
//        }
//    }
    
    private void drawStar(Star s) {
    	if (s == null) return;
    	List<Double> ptl = s.getPoints();
    	int size = ptl.size();
        if (ptl.size() < 4) return;
        gc.setStroke(s.getColor());
        gc.setLineWidth(s.getWidth());
        //gc.beginPath();
        //gc.moveTo(ptl.get(0), ptl.get(1));
        double rx = Math.min(ptl.get(0), ptl.get(size-2));
        double ry = Math.min(ptl.get(1), ptl.get(size-1));
        double rw = Math.abs(ptl.get(size-2) - ptl.get(0));
        double rh = Math.abs(ptl.get(size-1) - ptl.get(1));
        //gc.strokeRect(rx, ry, rw, rh);
        //gc.strokeRect(rx, ry, rw, rh);
     // центр и внешний радиус (по меньшей стороне прямоугольника)
        double cx = rx + rw / 2.0;
        double cy = ry + rh / 2.0;
        double outerRadius = Math.min(rw, rh) / 2.0;
        // внутренний радиус задаём как долю внешнего (0.4-0.5 обычно хорошо)
        double innerRadius = outerRadius * 0.5;

        // количество вершин: 5-конечная звезда -> 10 точек чередующихся
        int points = 10;
        double[] xs = new double[points];
        double[] ys = new double[points];

        // смещение угла так, чтобы один луч смотрел вверх (можно изменить)
        double startAngle = -Math.PI / 2.0; // вверх
        for (int i = 0; i < points; i++) {
            double angle = startAngle + i * (2 * Math.PI / points);
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            xs[i] = cx + Math.cos(angle) * r;
            ys[i] = cy + Math.sin(angle) * r;
        }

        // рисуем замкнутый контур
        gc.strokePolygon(xs, ys, points);
    }

    private void drawRotRect(RotRect rr) {
        if (rr == null) return;
        List<Double> ptl = rr.getPoints();
        int size = ptl.size();
        if (size < 4) return;

        gc.save(); // важно сохранить состояние перед трансформациями

        gc.setStroke(rr.getColor());
        gc.setLineWidth(rr.getWidth());

        double sx = ptl.get(0);
        double sy = ptl.get(1);
        double cx = ptl.get(size - 2);
        double cy = ptl.get(size - 1);

        double rx = Math.min(sx, cx);
        double ry = Math.min(sy, cy);
        double rw = Math.abs(cx - sx);
        double rh = Math.abs(cy - sy);
        double centerX = rx + rw / 2.0;
        double centerY = ry + rh / 2.0;
        double angle = Math.atan2(cy - sy, cx - sx);

        gc.translate(centerX, centerY);
        gc.rotate(Math.toDegrees(angle));
        gc.strokeRect(-rw / 2.0, -rh / 2.0, rw, rh);

        gc.restore(); // вернуть предыдущее состояние
    }
    
    private void drawRect(Rect r) {
    	if (r == null) return;
    	List<Double> ptl = r.getPoints();
    	int size = ptl.size();
        if (ptl.size() < 4) return;
        gc.setStroke(r.getColor());
        gc.setLineWidth(r.getWidth());
        gc.beginPath();
        gc.moveTo(ptl.get(0), ptl.get(1));
        double rx = Math.min(ptl.get(0), ptl.get(size-2));
        double ry = Math.min(ptl.get(1), ptl.get(size-1));
        double rw = Math.abs(ptl.get(size-2) - ptl.get(0));
        double rh = Math.abs(ptl.get(size-1) - ptl.get(1));
        //gc.strokeRect(rx, ry, rw, rh);
        gc.strokeRect(rx, ry, rw, rh);
    }
    private void drawCircle(Circle c) {
    	if (c == null) return;
    	List<Double> ptl = c.getPoints();
    	int size = ptl.size();
        if (ptl.size() < 4) return;
        gc.setStroke(c.getColor());
        gc.setLineWidth(c.getWidth());
        gc.beginPath();
        gc.moveTo(ptl.get(0), ptl.get(1));
        double rx = Math.min(ptl.get(0), ptl.get(size-2));
        double ry = Math.min(ptl.get(1), ptl.get(size-1));
        double rw = Math.abs(ptl.get(size-2) - ptl.get(0));
        double rh = Math.abs(ptl.get(size-1) - ptl.get(1));
        //gc.strokeRect(rx, ry, rw, rh);
        gc.strokeOval(rx, ry, rw, rh);
//        for (int i = 2; i < ptl.size(); i += 2) {
//            gc.lineTo(ptl.get(i), ptl.get(i + 1));
//        }
        //gc.lineTo(ptl.get(size-2), ptl.get(size-1));
        //gc.stroke();
    }
    
    private void drawLine(Line l) {
    	if (l == null) return;
    	List<Double> ptl = l.getPoints();
    	int size = ptl.size();
        if (ptl.size() < 4) return;
        gc.setStroke(l.getColor());
        gc.setLineWidth(l.getWidth());
        gc.beginPath();
        gc.moveTo(ptl.get(0), ptl.get(1));
//        for (int i = 2; i < ptl.size(); i += 2) {
//            gc.lineTo(ptl.get(i), ptl.get(i + 1));
//        }
        gc.lineTo(ptl.get(size-2), ptl.get(size-1));
        gc.stroke();
    }
 // рисовать штрихи
    private void drawStroke(Stroke s) {
        List<Double> pts = s.getPoints();
        if (pts.size() < 4) return;
        gc.setStroke(s.getColor());
        gc.setLineWidth(s.getWidth());
        gc.beginPath();
        gc.moveTo(pts.get(0), pts.get(1));
        for (int i = 2; i < pts.size(); i += 2) {
            gc.lineTo(pts.get(i), pts.get(i + 1));
        }
        gc.stroke();
    }
    // стирание
    private void eraseAt(double x, double y) {
    	final double ERASER_SIZE = 10;
    	gc.clearRect(x - ERASER_SIZE / 2, y - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
    }
    private void drawErase(Erase e) {
        List<Double> pts = e.getPoints();
        if (pts.size() < 4) return;
        gc.beginPath();
        gc.moveTo(pts.get(0), pts.get(1));
        for (int i = 2; i < pts.size(); i += 2) {
            eraseAt(pts.get(i), pts.get(i + 1));
        }
        gc.stroke();
    }
    private void redraw() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Drawable d : model) {
            if (d instanceof Stroke) {
                Stroke stroke = (Stroke) d;
                drawStroke(stroke);
            }
            else if (d instanceof Line) {
                Line line = (Line) d;
                drawLine(line);
            }
            else if (d instanceof Erase) {
                Erase eraser = (Erase) d;
                drawErase(eraser);
            }
            else if (d instanceof Circle) {
                Circle circle = (Circle) d;
                drawCircle(circle);
            }
            else if (d instanceof Rect) {
                Rect rect =(Rect) d;
                drawRect(rect);
            }
            else if (d instanceof Star) {
                Star star =(Star) d;
                drawStar(star);
            }
            else if (d instanceof RotRect) {
                RotRect rotRect =(RotRect) d;
                drawRotRect(rotRect);
            }
        }

    }
    // переместить в отдельный класс
    // Возвращает массив: {centerX, centerY, width, height, angleRad}
    private double[] calcRectParams(double sx, double sy, double cx, double cy) {
        double rx = Math.min(sx, cx);
        double ry = Math.min(sy, cy);
        double rw = Math.abs(cx - sx);
        double rh = Math.abs(cy - sy);
        double centerX = rx + rw / 2.0;
        double centerY = ry + rh / 2.0;
        // угол между вектором (sx,sy)->(cx,cy) и осью X
        double angle = Math.atan2(cy - sy, cx - sx); // радианы
        return new double[] {centerX, centerY, rw, rh, angle};
    }
    //Метод для рисования прямоугольника с поворотом
    private void drawRotatedRect(GraphicsContext gc, double centerX, double centerY,
            double w, double h, double angleRad) {
		gc.save();
		gc.translate(centerX, centerY);
		gc.rotate(Math.toDegrees(angleRad)); // rotate ожидает градусы
		gc.strokeRect(-w/2.0, -h/2.0, w, h); // рисуем от центра
		gc.restore();
	}
    private void drawPreviewRect(double sx, double sy, double x, double y) {
        restoreSnapshot(tempSnapshot);
        gc.setStroke(colorPicker.getValue());
        gc.setLineWidth(sizeSlider.getValue());
        double[] p = calcRectParams(sx, sy, x, y);
        drawRotatedRect(gc, p[0], p[1], p[2], p[3], p[4]);
    }
    
//    private void undo() {
//        if (undoStack.isEmpty()) return;
//        WritableImage current = canvas.snapshot(null, null);
//        redoStack.push(current);
//
//        WritableImage prev = undoStack.pop();
//        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//        gc.drawImage(prev, 0, 0);
//    }
//
//    private void redo() {
//        if (redoStack.isEmpty()) return;
//        WritableImage current = canvas.snapshot(null, null);
//        undoStack.push(current);
//
//        WritableImage next = redoStack.pop();
//        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//        gc.drawImage(next, 0, 0);
//    }

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
//    private void pushUndo() {
//        WritableImage snap = canvas.snapshot(null, null);
//        undoStack.push(snap);
//        System.out.println(undoStack);
//        // ограничение размера стека (опционально)
//        if (undoStack.size() > 50) {
//            // простая обрезка: удаляем самое старое (в данном простом примере не реализовано удаление нижнего элемента)
//        }
//    }
//    private void eraseAt(double x, double y) {
//        gc.clearRect(x - ERASER_SIZE / 2, y - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
//    }
    
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
                unManeger.pushUndo(canvas);
                //pushUndo();
                unManeger.clearRedoStack();
                //redoStack.clear();
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
