package application;

public interface Command {
    void execute();
    void undo();
}