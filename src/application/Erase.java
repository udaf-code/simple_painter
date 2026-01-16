package application;

import java.util.ArrayList;
import java.util.List;

public class Erase extends DrawableBase{
	private final List<Double> points = new ArrayList<>();
	public void addPoint(double x, double y) {
        points.add(x);
        points.add(y);
    }
    public List<Double> getPoints() {
        return points;
    }
}
