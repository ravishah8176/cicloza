package com.cicloza.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorLogger {
    private final Logger logger;
    
    private static final String ANSI_RESET = "\033[0m";
    
    // Colors for both level and messages
    private static final String COLOR_INFO = "\033[0;32m";     // Green
    private static final String COLOR_WARN = "\033[0;33m";     // Yellow
    private static final String COLOR_ERROR = "\033[0;31m";    // Red
    private static final String COLOR_DEBUG = "\033[0;34m";    // Blue
    
    // Bold versions for log levels
    private static final String LEVEL_INFO = "\033[1;32m";     // Bold Green
    private static final String LEVEL_WARN = "\033[1;33m";     // Bold Yellow
    private static final String LEVEL_ERROR = "\033[1;31m";    // Bold Red
    private static final String LEVEL_DEBUG = "\033[1;34m";    // Bold Blue

    private ColorLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static ColorLogger getLogger(Class<?> clazz) {
        return new ColorLogger(clazz);
    }

    public void info(String message, Object... args) {
        String coloredMessage = formatMessage(COLOR_INFO, message, args);
        logger.info(LEVEL_INFO + "[INFO]" + ANSI_RESET + " " + coloredMessage);
    }

    public void warn(String message, Object... args) {
        String coloredMessage = formatMessage(COLOR_WARN, message, args);
        logger.warn(LEVEL_WARN + "[WARN]" + ANSI_RESET + " " + coloredMessage);
    }

    public void error(String message, Object... args) {
        String coloredMessage = formatMessage(COLOR_ERROR, message, args);
        logger.error(LEVEL_ERROR + "[ERROR]" + ANSI_RESET + " " + coloredMessage);
    }

    public void debug(String message, Object... args) {
        String coloredMessage = formatMessage(COLOR_DEBUG, message, args);
        logger.debug(LEVEL_DEBUG + "[DEBUG]" + ANSI_RESET + " " + coloredMessage);
    }

    private String formatMessage(String color, String message, Object... args) {
        if (args == null || args.length == 0) {
            return color + message + ANSI_RESET;
        }

        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int lastIndex = 0;

        while (lastIndex < message.length()) {
            int placeholderIndex = message.indexOf("{}", lastIndex);
            if (placeholderIndex == -1 || argIndex >= args.length) {
                result.append(color)
                      .append(message.substring(lastIndex))
                      .append(ANSI_RESET);
                break;
            }

            // Add text before placeholder
            result.append(color)
                  .append(message.substring(lastIndex, placeholderIndex))
                  .append(ANSI_RESET);
            
            // Add the argument with the same color
            result.append(color)
                  .append(args[argIndex])
                  .append(ANSI_RESET);

            lastIndex = placeholderIndex + 2;
            argIndex++;
        }

        return result.toString();
    }
} 