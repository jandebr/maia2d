package org.maia.graphics2d.image.pool;

import java.awt.Image;
import java.util.Queue;

import org.maia.graphics2d.image.ImageInfo;
import org.maia.graphics2d.image.ImageInfoImpl;
import org.maia.util.AsyncSerialTaskWorker;
import org.maia.util.AsyncSerialTaskWorker.AsyncTask;
import org.maia.util.StringUtils;

public abstract class AbstractPooledImage implements PooledImage {

	private String imageIdentifier;

	private ImagePool imagePool;

	private ImageInfo imageInfo;

	private static ImageProductionTaskWorker imageProductionTaskWorker;

	static {
		imageProductionTaskWorker = new ImageProductionTaskWorker();
		imageProductionTaskWorker.start();
	}

	protected AbstractPooledImage(String imageIdentifier, ImagePool imagePool) {
		this(imageIdentifier, imagePool, new ImageInfoImpl());
	}

	protected AbstractPooledImage(String imageIdentifier, ImagePool imagePool, ImageInfo imageInfo) {
		this.imageIdentifier = imageIdentifier;
		this.imagePool = imagePool;
		this.imageInfo = imageInfo;
	}

	@Override
	public synchronized Image getImage() {
		String imageId = getImageIdentifier();
		Image image = getImagePool().fetchImage(imageId);
		if (image != null) {
			logOperation("GET-CACHED");
		} else if (!getImagePool().isVoidImage(imageId)) {
			logOperation("GET-PRODUCE");
			image = produceImage();
		} else {
			logOperation("GET-VOID");
		}
		return image;
	}

	@Override
	public synchronized Image requestImage() {
		String imageId = getImageIdentifier();
		Image image = getImagePool().fetchImage(imageId);
		if (image != null) {
			logOperation("REQUEST-CACHED");
		} else if (!getImagePool().isVoidImage(imageId)) {
			logOperation("REQUEST-PRODUCE");
			imageProductionTaskWorker.addTask(new ImageProductionTask());
		} else {
			logOperation("REQUEST-VOID");
		}
		return image;
	}

	@Override
	public synchronized Image probeImage() {
		String imageId = getImageIdentifier();
		Image image = getImagePool().fetchImage(imageId);
		if (image != null) {
			logOperation("PROBE-CACHED");
		} else {
			logOperation("PROBE-MISSING");
		}
		return image;
	}

	@Override
	public synchronized void disposeImage() {
		logOperation("DISPOSE");
		getImagePool().removeImage(getImageIdentifier());
	}

	private Image produceImage() {
		Image image = null;
		try {
			image = getImageProducer().produceImage(this);
			if (image != null) {
				getImagePool().storeImage(getImageIdentifier(), image);
			} else {
				getImagePool().addVoidImage(getImageIdentifier());
			}
		} catch (RetryablePooledImageProducerException e) {
			// not add as void, so it can be retried
		}
		return image;
	}

	private void logOperation(String operation) {
		if (PooledImageLogger.logInfoEnabled) {
			PooledImageLogger.logInfo(getImageIdentifier(), StringUtils.fitWidth(operation, 16));
		}
	}

	@Override
	public String getImageIdentifier() {
		return imageIdentifier;
	}

	@Override
	public ImagePool getImagePool() {
		return imagePool;
	}

	@Override
	public ImageInfo getImageInfo() {
		return imageInfo;
	}

	private class ImageProductionTask implements AsyncTask {

		public ImageProductionTask() {
		}

		@Override
		public void process() {
			PooledImageLogger.logDebug(getImageIdentifier(), "Producing image");
			produceImage();
		}

		public String getImageIdentifier() {
			return AbstractPooledImage.this.getImageIdentifier();
		}

	}

	/**
	 * Keeps a backlog of at most 1 image producer task (most recently added) + at most 1 task in progress
	 */
	private static class ImageProductionTaskWorker extends AsyncSerialTaskWorker<ImageProductionTask> {

		public ImageProductionTaskWorker() {
			super("Image Production task worker");
		}

		@Override
		protected void addTaskToQueue(ImageProductionTask task, Queue<ImageProductionTask> queue) {
			ImageProductionTask currentTask = queue.peek();
			queue.clear(); // discard any backlog
			if (currentTask != null) {
				queue.add(currentTask); // likely this task is in progress, keep it
			}
			if (currentTask == null || !currentTask.getImageIdentifier().equals(task.getImageIdentifier())) {
				queue.add(task);
				PooledImageLogger.logDebug(task.getImageIdentifier(), "Queueing image production");
			}
		}

	}

}