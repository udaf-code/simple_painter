package application;

import java.util.List;

public class DrawStarCommand implements Command{
	// общая модель <Drawable>
    private final List<Drawable> model;
    private final Star star;

    public DrawStarCommand(List<Drawable> model, Star star) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.star = star;
    }

    @Override
    public void execute() {
        model.add(star);
    }

    @Override
    public void undo() {
        model.remove(star);
    }
}
