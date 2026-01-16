package application;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Stroke extends DrawableBase{
    private final List<Double> points = new ArrayList<>(); // x0,y0,x1,y1,...
    private final Color color;
    private final double width;

    public Stroke(Color color, double width) {
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

    public Stroke copy() {
        Stroke s = new Stroke(color, width);
        s.points.addAll(this.points);
        return s;
    }
}