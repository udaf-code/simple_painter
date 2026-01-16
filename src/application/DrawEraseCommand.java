package application;

import java.util.List;

public class DrawEraseCommand implements Command{
	// общая модель <Drawable>
    private final List<Drawable> model;
    private final Erase eraser;
    
    public DrawEraseCommand(List<Drawable> model, Erase eraser) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.eraser = eraser;
    }

    @Override
    public void execute() {
        model.add(eraser);
    }

    @Override
    public void undo() {
        model.remove(eraser);
    }
}
