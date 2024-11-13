package org.unibl.etf.mdp.accountingserver.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.unibl.etf.mdp.accountingserver.properties.AppConfig;


public class FileLogger {
	public static Logger getLogger(String className) {
		Logger logger = Logger.getLogger(className);

		if (logger.getHandlers().length == 0) {
			try {
				AppConfig conf = new AppConfig();
				String logDir = conf.getLogDir();
				String logFile = conf.getLogFile();

				Path logDirPath = Paths.get(logDir);
				Path logFilePath = logDirPath.resolve(logFile);

				if (Files.notExists(logDirPath)) {
					Files.createDirectories(logDirPath);
				}

				FileHandler fileHandler = new FileHandler(logFilePath.toString(), true);
				fileHandler.setFormatter(new SimpleFormatter());
				logger.addHandler(fileHandler);
				logger.setLevel(Level.ALL);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to initialize file handler for logger", e);
			}
		}
		return logger;
	}
}
