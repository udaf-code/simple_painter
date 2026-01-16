package application;

import java.util.List;

public class DrawRectCommand implements Command{
	// общая модель <Drawable>
    private final List<Drawable> model;
    private final Rect rect;

    public DrawRectCommand(List<Drawable> model, Rect rect) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.rect = rect;
    }

    @Override
    public void execute() {
        model.add(rect);
    }

    @Override
    public void undo() {
        model.remove(rect);
    }
}
