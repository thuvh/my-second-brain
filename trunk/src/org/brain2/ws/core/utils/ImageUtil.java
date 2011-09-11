package org.brain2.ws.core.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

//1280 x 854 and 608 x 912, 75x75
public class ImageUtil {
	public static final String SMALL_PREFIX = "-smallphoto-";
	public static final String MEDIUM_PREFIX = "-mediumphoto-";
	public static final String THUMB_PREFIX = "-thumbphoto-";
	private static final int IMG_THUMB_SCALE_SIZE = 165;

	public static File resizeImage(File originalFile, File tempThumbFile) {
		// return resizeImage(IMG_THUMB_SCALE_SIZE, originalFile,
		// tempThumbFile);
		try {
			return resize(originalFile, tempThumbFile, IMG_THUMB_SCALE_SIZE, 0.96f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File resize(File originalFile, File resizedFile, int newWidth, float quality) throws IOException {
		if (quality < 0 || quality > 1) {
			throw new IllegalArgumentException("Quality has to be between 0 and 1");
		}

		ImageIcon ii = new ImageIcon(originalFile.getCanonicalPath());
		Image i = ii.getImage();
		Image resizedImage = null;

		int iWidth = i.getWidth(null);
		int iHeight = i.getHeight(null);

        if (iWidth > iHeight) {
            resizedImage = i.getScaledInstance(newWidth, (newWidth * iHeight) / iWidth, Image.SCALE_SMOOTH);
        } else {
        	if(newWidth == IMG_THUMB_SCALE_SIZE){
        		newWidth = 190;
        	}
            resizedImage = i.getScaledInstance((newWidth * iWidth) / iHeight, newWidth, Image.SCALE_SMOOTH);
        }

		// This code ensures that all the pixels in the image are loaded.
		Image temp = new ImageIcon(resizedImage).getImage();

		// Create the buffered image.
		BufferedImage bufferedImage = new BufferedImage(temp.getWidth(null), temp.getHeight(null),
				BufferedImage.SCALE_SMOOTH);

		// Copy image to buffered image.
		Graphics2D g = bufferedImage.createGraphics();

		// Clear background and paint the image.
		g.setColor(Color.white);
		g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
		g.drawImage(temp, 0, 0, null);
		g.dispose();

		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Soften.
		float softenFactor = 0.05f;
		float[] softenArray = { 0, softenFactor, 0, softenFactor, 1 - (softenFactor * 4), softenFactor, 0,
				softenFactor, 0 };
		Kernel kernel = new Kernel(3, 3, softenArray);
		ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		bufferedImage = cOp.filter(bufferedImage, null);

		ImageIO.write(bufferedImage, "jpg", resizedFile);

		// // Write the jpeg to a file.
		// FileOutputStream out = new FileOutputStream(resizedFile);
		// // Encodes image as a JPEG data stream
		// JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		// JPEGEncodeParam param =
		// encoder.getDefaultJPEGEncodeParam(bufferedImage);
		// param.setQuality(quality, true);
		// param.setXDensity(96) ;
		// param.setYDensity(96) ;
		// param.setDensityUnit(com.sun.image.codec.jpeg.JPEGDecodeParam.DENSITY_UNIT_DOTS_INCH);
		//
		// encoder.setJPEGEncodeParam(param);
		// encoder.encode(bufferedImage);
		// out.close();

		return resizedFile;
	}

	public static File resizeImage(int scaledSize, File originalFile, File scaledImageFile) {
		try {
			BufferedImage originalImage = ImageIO.read(originalFile);
			int w = originalImage.getWidth(), h = originalImage.getHeight();
			if (w >= h) {
				// landscape
				if (scaledSize == IMG_THUMB_SCALE_SIZE) {
					scaledSize = 350;
				}
				if (w > scaledSize) {
					h = (int) Math.ceil((h * scaledSize) / w);
					w = scaledSize;
				}
			} else {
				// portrait
				if (h > scaledSize) {
					w = (int) Math.ceil((w * scaledSize) / h);
					h = scaledSize;
				}
			}

			// int type = originalImage.getType() == 0 ?
			// BufferedImage.SCALE_SMOOTH: originalImage.getType();
			BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.SCALE_SMOOTH);
			Graphics2D g = resizedImage.createGraphics();
			g.setComposite(AlphaComposite.Src);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(originalImage, 0, 0, w, h, null);
			g.dispose();

			ImageIO.write(resizedImage, "jpg", scaledImageFile);
			return scaledImageFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String dir = "D:/";
		File tempPhotoFile = new File(dir + "84587d84ff7d802ce052d57132e1a007a4e19a44.jpg");

		File tempThumbPhotoFile = new File(dir + "84587d84ff7d802ce052d57132e1a007a4e19a44_thum.jpg");
		tempThumbPhotoFile = ImageUtil.resizeImage(IMG_THUMB_SCALE_SIZE, tempPhotoFile, tempThumbPhotoFile);
		System.out.println("OK @ " + tempThumbPhotoFile.getAbsolutePath());

		File tempPhotoFile2 = new File(dir + "2011-04-10_17-21-05_840.jpg");
		File tempThumbPhotoFile2 = new File(dir + "2011-04-10_17-21-05_840_thum.jpg");
		tempThumbPhotoFile = ImageUtil.resizeImage(IMG_THUMB_SCALE_SIZE, tempPhotoFile2, tempThumbPhotoFile2);
		System.out.println("OK @ " + tempThumbPhotoFile.getAbsolutePath());

		// int a = 0xbe37, b = 0xface;
		// int c = (a ^ b) << 4;
		// System.out.println(c);
	}
}
