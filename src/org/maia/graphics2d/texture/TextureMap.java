package org.maia.graphics2d.texture;

import java.awt.Color;

public interface TextureMap {

	double sampleDouble(double x, double y);

	int sampleInt(double x, double y);

	Color sampleColor(double x, double y);

}
