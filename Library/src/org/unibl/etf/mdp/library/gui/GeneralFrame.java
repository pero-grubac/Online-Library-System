package org.unibl.etf.mdp.library.gui;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;



public class GeneralFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	protected static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(GeneralFrame.class.getName());

	public GeneralFrame(String title) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		try {
			setTitle(title);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred in the client application", e);
		}
		setSize(new Dimension(100, 100));
		setLocationRelativeTo(null);
	}
}
