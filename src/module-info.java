module paintApp {
	requires javafx.controls;
	requires javafx.swing;
	requires javafx.graphics;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	
	opens application to javafx.graphics, javafx.fxml;
}
