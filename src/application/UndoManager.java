package application;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final int maxDepth;

    public UndoManager(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void doCommand(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        trimToDepth();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
        	
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
            
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
            trimToDepth();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    private void trimToDepth() {
        while (undoStack.size() > maxDepth) {
            // удаляем самый старый — у ArrayDeque нет прямого удаления "низу" в O(1) кроме removeLast
            undoStack.removeLast();
        }
    }
}