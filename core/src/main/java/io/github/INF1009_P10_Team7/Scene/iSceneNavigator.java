package io.github.INF1009_P10_Team7.Scene;

public interface iSceneNavigator {
	void setScene(Scene scene);
    void pushScene(Scene scene);
    void popScene();
}
