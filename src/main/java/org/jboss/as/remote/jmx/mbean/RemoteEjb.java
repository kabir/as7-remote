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
package org.jboss.as.remote.jmx.mbean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.IllegalStateException;

import org.jboss.as.remote.jmx.common.MethodUtil;
import org.jboss.logging.Logger;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class RemoteEjb implements RemoteEjbMBean {

    Logger log = Logger.getLogger(RemoteEjb.class);

    Map<String, Object> statelessBeans = Collections.synchronizedMap(new HashMap<String, Object>());

    public Integer lookup(String className, String name) throws NamingException {
        return lookupStateless(className, name);
    }

    private Integer lookupStateless(String className, String name) throws NamingException {
        System.out.println("Looking up bean");
        Object value = statelessBeans.get(name);
        if (value != null) {
            return 0;
        }
        InitialContext context = new InitialContext();
        value = context.lookup(name);
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            NamingException ex = new NamingException("Could not find class for " + className + " in loader: "  + e.getMessage());
            ex.setRootCause(e);
            throw ex;
        }
        System.out.println(clazz);
        System.out.println(value.getClass());

        System.out.println(value.getClass().isAssignableFrom(clazz));
        System.out.println(clazz.isAssignableFrom(value.getClass()));

        if (!clazz.isAssignableFrom(value.getClass())) {
            throw new NamingException("Expected " + className + " for " + name);
        }
        statelessBeans.put(name, value);
        return -1;
    }

    @Override
    public Object invokeStateless(String name, String methodName, String[] sig, Object[] args) throws Exception {
        System.out.println("Invoking on bean");
        Object value = statelessBeans.get(name);
        if (value == null) {
            throw new IllegalStateException("No proxy found for: " + name);
        }

        Method m;
        try {
            m = MethodUtil.getMethod(value.getClass(), methodName, sig);
        } catch (Exception e) {
            throw new RuntimeException("Could not find method called " + methodName + " with signature " + Arrays.toString(sig));
        }

        return m.invoke(value, args);
    }

    @Override
    public void start() {
        log.info("Starting remote ejb invocation mbean");
    }

}
