package io.github.INF1009_P10_Team7.InputOutput;

public interface iInputController {
	boolean isActionPressed(String actionName);
    boolean isActionJustPressed(String actionName);
    void bindKey(String actionName, int keyCode);
    void bindMouse(String actionName, int mouseButton);
    int getKeyCode(String actionName);
}
