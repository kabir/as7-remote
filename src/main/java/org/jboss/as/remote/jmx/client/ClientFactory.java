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

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.jboss.as.remote.jmx.mbean.RemoteViaJMX;

/**
 * Factory to create clients to look up things in JNDI and invoke upon EJBs via JMX.
 * For this to work your application must deploy the {@link RemoteViaJMX} MBean.
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ClientFactory {

    public static final ClientFactory INSTANCE = new ClientFactory();

    Map<ObjectName, Client> clients = new HashMap<ObjectName, Client>();

    /**
     * Gets a client via the object name of the registered {@link RemoteViaJMX} MBean.
     * This client can be reused until it is no longer needed, as long as the MBean is deployed
     *
     * @param appName the object name of the {@link RemoteViaJMX} MBean the client is communicating with
     * @return a client that can be used to lookup things in JNDI and invoke upon EJBs or null if it does not exist.
     */
    public synchronized Client getClient(ObjectName appName) {
        return clients.get(appName);
    }

    /**
     * Gets a client via the object name of the registered {@link RemoteViaJMX} MBean.
     * This client can be reused until it is no longer needed, as long as the MBean is deployed.
     * If the client does not already exist it will be created
     *
     *
     * @param appName the object name of the {@link RemoteViaJMX} MBean the client is communicating with
     * @param host the host name of the application server. If {@code null} it defaults to {@code localhost}
     * @param port the port of the jmx connector on the application server. If {@code <=0} it defaults to {@code 1090}
     * @return a client that can be used to lookup things in JNDI and invoke upon EJBs
     */
    public synchronized Client getOrCreateClient(ObjectName appName, String host, int port) {
        Client client = clients.get(appName);
        if (client == null) {
            client = new Client(this, appName, host, port);
            clients.put(appName, client);
        }
        return client;
    }

    /**
     * Closes a client instance
     *
     * @param the object name of the {@link RemoteViaJMX} MBean the client is communicating with
     */
    synchronized void closeClient(ObjectName appName) {
        Client client = clients.remove(appName);
        if (client != null) {
            client.safeClose();
        }
    }
}
