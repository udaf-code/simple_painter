package application;

import java.util.Deque;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

public class undoManager {
	private boolean is_snapshot;
	private void pushUndo(WritableImage snap, Deque<WritableImage> undoStack, Canvas canvas) {
		snap = canvas.snapshot(null, null);
        undoStack.push(snap);
        System.out.println(undoStack);
        // ограничение размера стека (опционально)
        if (undoStack.size() > 50) {
            // простая обрезка: удаляем самое старое (в данном простом примере не реализовано удаление нижнего элемента)
        }
	}
	private void undo(Deque<WritableImage> undoStack, Canvas canvas, Deque<WritableImage> redoStack, GraphicsContext gc) {
		if (undoStack.isEmpty()) return;
        WritableImage current = canvas.snapshot(null, null);
        redoStack.push(current);

        WritableImage prev = undoStack.pop();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(prev, 0, 0);
	}
	private void redo(Deque<WritableImage> undoStack, Canvas canvas, Deque<WritableImage> redoStack, GraphicsContext gc) {
		if (redoStack.isEmpty()) return;
        WritableImage current = canvas.snapshot(null, null);
        undoStack.push(current);

        WritableImage next = redoStack.pop();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(next, 0, 0);
	}
	
}
