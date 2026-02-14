package io.github.INF1009_P10_Team7.engine.inputoutput;

import com.badlogic.gdx.Gdx;

/**
 * A concrete implementation of {@link DeviceInput} for Mouse handling.
 * <p>
 * This class manages:
 * <ul>
 * <li><b>Buttons:</b> Standard clicks (Left, Right, Middle, etc.).</li>
 * <li><b>Axes:</b> The X and Y screen coordinates of the cursor.</li>
 * </ul>
 * Like the keyboard, it maintains state history to allow for "Just Pressed" (click) detection.
 */
public class MouseDevice extends DeviceInput {
	
    private float mouseX;
    private float mouseY;
    
    /** 
     * Stores the state of mouse buttons for the current frame.
     */
    private boolean[] currentButtons = new boolean[5];
    
    /**
     * Stores the state of mouse buttons from the previous frame. 
     */
    private boolean[] previousButtons = new boolean[5];
    
    /**
     * Initializes the mouse device with ID 1.
     */
    public MouseDevice() {
        this.deviceID = 1;
        this.deviceName = "Mouse";
    }

    /**
     * Updates the internal mouse state.
     * <p>
     * Logic:
     * <ol>
     * <li>Updates {@code mouseX} and {@code mouseY} from Gdx.input.</li>
     * <li>Copies the current button states to the previous array (snapshot).</li>
     * <li>Polls the new button states from LibGDX.</li>
     * </ol>
     */
    @Override
    public void pollInput() {
        // Get and update current X,Y coordinate of mouse
        mouseX = Gdx.input.getX();
        mouseY = Gdx.input.getY();

        // Update buttons pressed
        // First, copy current state to previous state (Critical for JustPressed logic)
        System.arraycopy(currentButtons, 0, previousButtons, 0, 5);

        // Then, read new state from LibGDX hardware
        for (int i = 0; i < 5; i++) {
            currentButtons[i] = Gdx.input.isButtonPressed(i);
        }
    }

    /**
     * Checks if a mouse button is currently held down.
     * @param id The Button Code (0=Left, 1=Right, 2=Middle).
     * @return {@code true} if the button is down.
     */
    @Override
    public boolean getButton(int id) {
    	if (id >= 0 && id < 5) {
            return currentButtons[id];
        }
        return false;
    }

    /**
     * Checks if a mouse button was clicked <b>this exact frame</b>.
     * @param id The Button Code.
     * @return {@code true} if the button transitioned from UP to DOWN this frame.
     */
    @Override
    public boolean isButtonJustPressed(int id) {
        if (id >= 0 && id < 5) {
            return currentButtons[id] && !previousButtons[id];
        }
        return false;
    }

    /**
     * Retrieves the current position of the mouse cursor.
     * <p>
     * Coordinates are typically in "Screen Space" (pixels), where (0,0) is usually
     * the top-left corner of the window.
     * @param id The Axis ID. <b>0</b> for X-axis, <b>1</b> for Y-axis.
     * @return The coordinate value.
     */
    @Override
    public float getAxis(int id) {
        if (id == 0) return mouseX;
        if (id == 1) return mouseY;
        return 0;
    }
}