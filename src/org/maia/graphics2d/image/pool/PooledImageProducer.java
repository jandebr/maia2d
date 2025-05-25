package org.maia.graphics2d.image.pool;

import java.awt.Image;

public interface PooledImageProducer {

	Image produceImage(PooledImage pooledImage);

}