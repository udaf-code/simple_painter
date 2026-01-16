package application;

import java.util.List;

public class DrawStrokeCommand implements Command {
	// общая модель <Drawable>
    private final List<Drawable> model;
    private final Stroke stroke;

    public DrawStrokeCommand(List<Drawable> model, Stroke stroke) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.stroke = stroke.copy();
    }

    @Override
    public void execute() {
        model.add(stroke);
    }

    @Override
    public void undo() {
        model.remove(stroke);
    }
}
