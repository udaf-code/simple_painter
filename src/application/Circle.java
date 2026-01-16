package application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class Circle extends DrawableBase{
	private final List<Double> points = new ArrayList<>();
	private final Color color;
	private final double width;
	public Circle(Color color, double width) {
		this.color = color;
		this.width = width;
	}
	public void addPoint(double x, double y) {
        points.add(x);
        points.add(y);
    }
    public List<Double> getPoints() {
        return points;
    }
    public Color getColor() {
        return color;
    }

    public double getWidth() {
        return width;
    }
	
}
