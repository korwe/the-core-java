/*
 * Copyright (c) 2010.  Korwe Software
 *
 *  This file is part of TheCore.
 *
 *  TheCore is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TheCore is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with TheCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.korwe.thecore.api;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author jacobus
 */
public class CoreConfig {

    private static final Logger LOG = Logger.getLogger(CoreConfig.class);

    private static Properties prop = new Properties();

    static {
        FileInputStream fileInputStream;
        try {
            File configFile = new File("coreconfig.xml");
            LOG.info("configFile.getAbsolutePath() = " + configFile.getAbsolutePath());
            fileInputStream = new FileInputStream(configFile);
            prop.loadFromXML(fileInputStream);
        }
        catch (Exception e) {
            createDefaultSettingsFile();
            LOG.error("Config file not found, created default corefonfig.xml. Please edit and rerun.");
        }
    }

    public static String getSetting(String settingName) {
        return prop.getProperty(settingName);
    }

    public static int getIntSetting(String settingName) {
        String setting = getSetting(settingName);
        try {
            return Integer.parseInt(setting);
        }
        catch (NumberFormatException e) {
            LOG.error("Invalid integer setting for " + settingName, e);
            return 0;
        }
    }

    public static void createDefaultSettingsFile() {
        Properties prop = new Properties();
        prop.setProperty("amqp_server", "localhost");
        prop.setProperty("amqp_port", "5672");
        prop.setProperty("amqp_vhost", "/");
        prop.setProperty("amqp_user", "guest");
        prop.setProperty("amqp_password", "guest");
        prop.setProperty("session_message_filter", "#");
        prop.setProperty("processor_type", "com.korwe.thecore.session.BasicMessageProcessor");
        prop.setProperty("timeout_seconds", "1800");
        prop.setProperty("scxml_path", "core_session.scxml");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("coreconfig.xml");
            prop.storeToXML(fileOutputStream, "TheCore config file");
            fileOutputStream.close();
        }
        catch (Exception e) {
            LOG.error("Could not create coreconfig file", e);
        }
    }
}
