package com.github.datalking.common.convert.editor;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

/**
 */
public class InputStreamEditor extends PropertyEditorSupport {
//
//	private final ResourceEditor resourceEditor;
//
//
//	/**
//	 * Create a new InputStreamEditor,
//	 * using the default ResourceEditor underneath.
//	 */
//	public InputStreamEditor() {
//		this.resourceEditor = new ResourceEditor();
//	}
//
//	/**
//	 * Create a new InputStreamEditor,
//	 * using the given ResourceEditor underneath.
//	 * @param resourceEditor the ResourceEditor to use
//	 */
//	public InputStreamEditor(ResourceEditor resourceEditor) {
//		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
//		this.resourceEditor = resourceEditor;
//	}
//
//
//	@Override
//	public void setAsText(String text) throws IllegalArgumentException {
//		this.resourceEditor.setAsText(text);
//		Resource resource = (Resource) this.resourceEditor.getValue();
//		try {
//			setValue(resource != null ? resource.getInputStream() : null);
//		}
//		catch (IOException ex) {
//			throw new IllegalArgumentException(
//					"Could not retrieve InputStream for " + resource + ": " + ex.getMessage());
//		}
//	}

    @Override
    public String getAsText() {
        return null;
    }

}
