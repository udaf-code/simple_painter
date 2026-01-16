package application;

import java.util.List;

public class DrawCircleCommand implements Command{
	// общая модель <Drawable>
    private final List<Drawable> model;
    private final Circle circle;

    public DrawCircleCommand(List<Drawable> model, Circle circle) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.circle = circle;
    }

    @Override
    public void execute() {
        model.add(circle);
    }

    @Override
    public void undo() {
        model.remove(circle);
    }
}
