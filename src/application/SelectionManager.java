package application;

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;

public class SelectionManager {
	String ACTIVE_CLASS = "active";
	private Node active;

    void register(Node node) {
        node.setOnMouseClicked(e -> select(node));
        // Для контролов, которые не реагируют мышью (например, ColorPicker),
        // можно добавить слушатель valueProperty или onAction:
        if (node instanceof ColorPicker) {
            ((ColorPicker) node).valueProperty().addListener((o, ov, nv) -> select(node));
        }
        if (node instanceof Slider) {
            ((Slider) node).valueProperty().addListener((o, ov, nv) -> {
                // опционально: считать активным при изменении
                select(node);
            });
        }
    }

    void select(Node node) {
        if (active != null) active.getStyleClass().remove(ACTIVE_CLASS);
        active = node;
        if (active != null && !active.getStyleClass().contains(ACTIVE_CLASS)) {
            active.getStyleClass().add(ACTIVE_CLASS);
        }
    }

    Node getActive() { return active; }
}

