package org.maia.graphics2d.image;

public class ImageInfoImpl implements ImageInfo {

	private String title;

	private String description;

	public ImageInfoImpl() {
		this(null);
	}

	public ImageInfoImpl(String title) {
		this(title, null);
	}

	public ImageInfoImpl(String title, String description) {
		this.title = title;
		this.description = description;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}