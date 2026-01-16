package application;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

public class UndoManagerSnap {
	//private boolean is_snapshot;
    private final Deque<WritableImage> undoStack = new ArrayDeque<>();
    private final Deque<WritableImage> redoStack = new ArrayDeque<>();
//	private final int maxSize;
//
//    public UndoManager(int maxSize) { this.maxSize = maxSize; }
    public void clearRedoStack() {
    	redoStack.clear();
    }
	public void pushUndo(Canvas canvas) {
		WritableImage snap = canvas.snapshot(null, null);
        undoStack.push(snap);
        System.out.println(undoStack);
        // ограничение размера стека (опционально)
        if (undoStack.size() > 50) {
            // простая обрезка: удаляем самое старое (в данном простом примере не реализовано удаление нижнего элемента)
        }
	}
	public void undo(Canvas canvas, GraphicsContext gc) {
		if (undoStack.isEmpty()) return;
        WritableImage current = canvas.snapshot(null, null);
        redoStack.push(current);

        WritableImage prev = undoStack.pop();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(prev, 0, 0);
	}
	public void redo(Canvas canvas, GraphicsContext gc) {
		if (redoStack.isEmpty()) return;
        WritableImage current = canvas.snapshot(null, null);
        undoStack.push(current);

        WritableImage next = redoStack.pop();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(next, 0, 0);
	}
	
}
