/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.connection;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.neo4j.neoclipse.util.ApplicationUtil;

/**
 * Our Neo4JConnection, which adds the connection to our GraphDatabaseService object
 * 
 * @author Radhakrishna Kalyan
 */
public class Alias {

    /* package */static final String ALIASES = "aliases";
    /* package */static final String ALIAS = "alias";
    /* package */static final String NAME = "name";
    /* package */static final String URI = "uri";
    /* package */static final String USER_NAME = "user-name";
    /* package */static final String PASSWORD = "password";
    /* package */static final String CONFIGURATIONS = "configurations";
    /* package */static final String CONFIG = "config";
    /* package */static final String CONFIG_NAME = "name";
    /* package */static final String CONFIG_VALUE = "value";

    private final String name;
    private String uri;
    private String userName;
    private String password;
    private long createdTime;
    private final ConnectionMode connectionMode;
    private final Map<String, String> configurationMap = new HashMap<String, String>();

    public Alias(final String aliasName, final String dbPath, final String user, final String pass)

    {

        name = aliasName;
        uri = dbPath;
        connectionMode = ConnectionMode.getValue(dbPath);

        if (connectionMode == ConnectionMode.LOCAL) {
            final File dir = new File(uri);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IllegalArgumentException("Could not create directory: " + dir);
                }
                uri = dir.getAbsolutePath();
            }
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("The database location is not a directory.");
            }
            if (!dir.canWrite()) {
                throw new IllegalAccessError("Permission Denied for write to the database location.");
            }
        }

        if (!ApplicationUtil.isBlank(user)) {
            userName = user;
        }
        if (!ApplicationUtil.isBlank(user)) {
            password = pass;
        }

        createdTime = System.currentTimeMillis();
    }

    /**
     * Constructs an Alias from XML, previously obtained from describeAsXml()
     * 
     * @param root
     */
    public Alias(final Element root) {
        name = root.elementText(NAME);
        uri = root.elementText(URI);
        connectionMode = ConnectionMode.getValue(uri);
        final String user = root.elementText(USER_NAME);
        final String pass = root.elementText(PASSWORD);
        if (!ApplicationUtil.isBlank(user)) {
            userName = user;
        }
        if (!ApplicationUtil.isBlank(pass)) {
            password = pass;
        }

        final Element configurationsElement = root.element(CONFIGURATIONS);
        if (configurationsElement != null) {
            final List<Element> elements = configurationsElement.elements(CONFIG);
            for (final Element config : elements) {
                final String configName = config.attributeValue(CONFIG_NAME);
                final String configValue = config.attributeValue(CONFIG_VALUE);
                addConfiguration(configName, configValue);
            }
        }
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    /**
     * Describes this alias in XML; the result can be passed to the Alias(Element) constructor to
     * refabricate it
     * 
     * @return
     */
    public Element describeAsXml() {
        final DefaultElement root = new DefaultElement(ALIAS);
        root.addElement(NAME).setText(ApplicationUtil.returnEmptyIfBlank(name));
        root.addElement(URI).setText(ApplicationUtil.returnEmptyIfBlank(uri));
        root.addElement(USER_NAME).setText(ApplicationUtil.returnEmptyIfBlank(userName));
        root.addElement(PASSWORD).setText(ApplicationUtil.returnEmptyIfBlank(password));

        if (!configurationMap.isEmpty()) {
            final Element configElement = root.addElement(CONFIGURATIONS);
            final Set<Entry<String, String>> entrySet = configurationMap.entrySet();
            for (final Entry<String, String> entry : entrySet) {
                final DefaultElement config = new DefaultElement(CONFIG);
                config.addAttribute(CONFIG_NAME, ApplicationUtil.returnEmptyIfBlank(entry.getKey()));
                config.addAttribute(CONFIG_VALUE, ApplicationUtil.returnEmptyIfBlank(entry.getValue()));
                configElement.add(config);
            }
        }
        return root;
    }

    public Map<String, String> getConfigurationMap() {
        return configurationMap;
    }

    public void addConfiguration(final String key, final String value) {
        configurationMap.put(key, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Alias other = (Alias)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }

    public String getConfigurationByKey(final String key) {
        return configurationMap.get(key);
    }
}
