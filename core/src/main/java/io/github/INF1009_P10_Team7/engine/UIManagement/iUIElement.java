package io.github.INF1009_P10_Team7.engine.UIManagement;

/**
 * iUIElement, root interface for all engine UI elements.
 *
 * <p>Defines the minimal render contract that every UI widget must satisfy.
 * Naming follows the lower-case 'i' convention used elsewhere in this
 * project for interfaces.</p>
 *
 * <p><b>Note on naming:</b> Java convention recommends upper-case 'I' for
 * interface names (e.g. {@code IUIElement}). The lower-case 'i' here is
 * a project-specific style choice; be consistent throughout the codebase.</p>
 */

/**
 * interface for UI Actions.
 */
public interface iUIElement {
    void execute();
}