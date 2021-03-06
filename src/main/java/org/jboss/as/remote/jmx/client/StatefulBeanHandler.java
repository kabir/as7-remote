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

import java.lang.reflect.Method;

import org.jboss.as.remote.jmx.common.MethodUtil;

public class StatefulBeanHandler extends ClientBeanHandler {
    private static final long serialVersionUID = 1L;
    private static final String[] INVOKE_SIGNATURE = new String[] {String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), Long.TYPE.getName(), String[].class.getName(), Object[].class.getName()};

    private final long sessionId;

    public StatefulBeanHandler(String name, long sessionId) {
        super(name);
        this.sessionId = sessionId;
    }

    @Override
    Object doInvoke(Object proxy, Client client, String name, Method method, Object[] args) throws Throwable {
        String[] sig = MethodUtil.getSignature(method);
        return client.getConnection().invoke(client.getAppMBeanName(), "invokeStateful", new Object[] {name, method.getDeclaringClass().getName(),method.getReturnType().getName(),  method.getName(), sessionId, sig, args}, INVOKE_SIGNATURE);
    }

}