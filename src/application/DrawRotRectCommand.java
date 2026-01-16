package application;

import java.util.List;

public class DrawRotRectCommand implements Command{
	// общая модель <Drawable>
    private final List<Drawable> model;
    private final RotRect rotRect;

    public DrawRotRectCommand(List<Drawable> model, RotRect rotRect) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.rotRect = rotRect;
    }

    @Override
    public void execute() {
        model.add(rotRect);
    }

    @Override
    public void undo() {
        model.remove(rotRect);
    }
}
