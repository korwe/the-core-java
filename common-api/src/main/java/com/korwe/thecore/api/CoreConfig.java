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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author jacobus
 */
public class CoreConfig extends Properties{

    private static final Logger LOG = LoggerFactory.getLogger(CoreConfig.class);

    private static CoreConfig instance;
    private static String configFilePath = "/coreconfig.xml";


    private CoreConfig() {
        try {
            LOG.info("Loading config file: "+configFilePath);
            if(configFilePath.startsWith("file:")){
                loadFromXML(new FileInputStream(configFilePath.substring(5)));
            }
            else{
                loadFromXML(this.getClass().getResourceAsStream(configFilePath));
            }
        }
        catch (Exception e) {
            createDefaultSettings();
            LOG.error("Config file not found, using defaults", e);
        }
    }

    public static synchronized CoreConfig getInstance() {
        if (null == instance) {
            instance = new CoreConfig();
        }
        return instance;
    }

    public int getIntProperty(String settingName) {
        try {
            return Integer.parseInt(getProperty(settingName));
        }
        catch (NumberFormatException e) {
            LOG.error("Invalid integer setting for " + settingName, e);
            return 0;
        }
    }

    private void createDefaultSettings() {
        setProperty("amqp_server", "localhost");
        setProperty("amqp_port", "5672");
        setProperty("amqp_vhost", "/");
        setProperty("amqp_user", "guest");
        setProperty("amqp_password", "guest");
        setProperty("session_message_filter", "#");
        setProperty("processor_type", "com.korwe.thecore.session.BasicMessageProcessor");
        setProperty("timeout_seconds", "1800");
        setProperty("scxml_path", "core_session.scxml");
        setProperty("max_threads", "16");
    }

    public static String getConfigFilePath() {
        return configFilePath;
    }

    public static void setConfigFilePath(String newConfigFilePath) {
        configFilePath = newConfigFilePath;
    }
}
