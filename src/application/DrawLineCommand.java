package application;

import java.util.List;

public class DrawLineCommand implements Command{
	private final List<Drawable> model;
	private final Line line;
	
	public DrawLineCommand(List<Drawable> model, Line line) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.line = line;
    }
	@Override
    public void execute() {
		model.add(line);
    }
	@Override
    public void undo() {
		model.remove(line);
    }
}
