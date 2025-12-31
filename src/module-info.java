module paintApp {
	requires javafx.controls;
	requires javafx.swing;
	
	opens application to javafx.graphics, javafx.fxml;
}
