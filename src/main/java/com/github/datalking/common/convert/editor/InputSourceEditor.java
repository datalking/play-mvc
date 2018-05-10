package com.github.datalking.common.convert.editor;

import org.xml.sax.InputSource;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

/**
 */
public class InputSourceEditor extends PropertyEditorSupport {

//	private final ResourceEditor resourceEditor;
//
//
//	/**
//	 * Create a new InputSourceEditor,
//	 * using the default ResourceEditor underneath.
//	 */
//	public InputSourceEditor() {
//		this.resourceEditor = new ResourceEditor();
//	}
//
//	/**
//	 * Create a new InputSourceEditor,
//	 * using the given ResourceEditor underneath.
//	 * @param resourceEditor the ResourceEditor to use
//	 */
//	public InputSourceEditor(ResourceEditor resourceEditor) {
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
//			setValue(resource != null ? new InputSource(resource.getURL().toString()) : null);
//		}
//		catch (IOException ex) {
//			throw new IllegalArgumentException(
//					"Could not retrieve URL for " + resource + ": " + ex.getMessage());
//		}
//	}

	@Override
	public String getAsText() {
		InputSource value = (InputSource) getValue();
		return (value != null ? value.getSystemId() : "");
	}

}
