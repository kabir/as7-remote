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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
abstract class ClientBeanHandler implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private volatile Client client;

    public ClientBeanHandler(String name) {
        this.name = name;
    }

    void setClient(Client client) {
        this.client = client;
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return doInvoke(proxy, client, name, method, args);
    }

    abstract Object doInvoke(Object proxy, Client client, String name, Method method, Object[] args) throws Throwable;

}
