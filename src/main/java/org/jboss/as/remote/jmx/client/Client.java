/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.remote.jmx.client;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class Client {

    private static String[] LOOKUP_SIG = new String[] {String.class.getName(), String.class.getName()};

    private Map<String, Object> proxies = new HashMap<String, Object>();

    private final ObjectName appMBeanName;
    private final String host;
    private final int port;
    private volatile JMXConnector jmxConnector;

    Client(ObjectName appMBeanName, String host, int port) {
        this.appMBeanName = appMBeanName;
        this.host = host;
        this.port = port;
    }

    public synchronized <T> T lookup(Class<T> clazz, String name) throws NamingException {
        Object client = proxies.get(name);
        if (client == null) {
            client = createProxy(clazz, name);
            proxies.put(name, client);
        }
        return clazz.cast(client);
    }

    private <T> T createProxy(Class<T> clazz, String name) throws NamingException {
        Integer type = null;
        try {
            type = (Integer)getConnection().invoke(appMBeanName, "lookup", new Object[] {clazz.getName(), name}, LOOKUP_SIG);
        } catch (Exception e) {
            if (e.getCause() instanceof NamingException) {
                throw (NamingException)e.getCause();
            }
            throw new RuntimeException(e);
        }
        StatelessBeanHandler handler = new StatelessBeanHandler(this, clazz, name);
        return (T)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {clazz}, handler);
    }

    void safeClose() {
        try {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        } catch (IOException ignore) {
        }
    }

    MBeanServerConnection getConnection() {
        String urlString = System.getProperty("jmx.service.url", "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        try {
            if (jmxConnector == null) {
                JMXServiceURL serviceURL = new JMXServiceURL(urlString);
                jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
            }
            return jmxConnector.getMBeanServerConnection();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + urlString, ex);
        }
    }

    ObjectName getAppMBeanName() {
        return appMBeanName;
    }
}