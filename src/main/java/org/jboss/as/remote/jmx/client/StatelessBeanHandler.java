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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.as.remote.jmx.common.MethodUtil;

class StatelessBeanHandler implements InvocationHandler {
    private static final String[] INVOKE_SIGNATURE = new String[] {String.class.getName(), String.class.getName(), String[].class.getName(), Object[].class.getName()};
    private final Client client;
    Class<?> iface;
    private final String name;

    public StatelessBeanHandler(Client client, Class<?> iface, String name) {
        this.client = client;
        this.iface = iface;
        this.name = name;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        String[] sig = MethodUtil.getSignature(method);
        System.out.println("Method: " + methodName);
        System.out.println("Sig: " + Arrays.toString(sig));

        return client.getConnection().invoke(client.getAppMBeanName(), "invokeStateless", new Object[] {name, method.getName(), sig, args}, INVOKE_SIGNATURE);
    }

}