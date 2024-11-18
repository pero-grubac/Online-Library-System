package org.unibl.etf.mdp.supplierserver.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.unibl.etf.mdp.supplierserver.logger.FileLogger;
import org.unibl.etf.mdp.supplierserver.properties.AppConfig;

public class ImageService {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ImageService.class.getName());

	public static BufferedImage downloadImage(String photoURL) {
		try {
			URL url = new URL(photoURL);

			BufferedImage img = ImageIO.read(url);
			return img;
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "An error occurred in the server application while downloading the picture", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "An error occurred in the server application while downloading the picture", e);
		}
		return null;
	}

}
