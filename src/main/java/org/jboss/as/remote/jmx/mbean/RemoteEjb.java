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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.as.remote.jmx.client.StatefulBeanHandler;
import org.jboss.as.remote.jmx.client.StatelessBeanHandler;
import org.jboss.as.remote.jmx.common.MethodUtil;
import org.jboss.logging.Logger;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class RemoteEjb implements RemoteEjbMBean {

    private final Logger log = Logger.getLogger(RemoteEjb.class);

    private final DeploymentReflectionIndex index = DeploymentReflectionIndex.create();

    private final Map<String, Object> statelessBeans = Collections.synchronizedMap(new HashMap<String, Object>());
    private final Map<Long, Object> statefulBeanInstances = Collections.synchronizedMap(new HashMap<Long, Object>());

    private final Set<String> statelessBeanNames = Collections.synchronizedSet(new HashSet<String>());
    private final Set<String> statefulBeanNames = Collections.synchronizedSet(new HashSet<String>());

    public Object lookup(String className, String name) throws NamingException {
        if (statelessBeanNames.contains(name)) {
            return lookupStateless(className, name);
        }
        if (statefulBeanNames.contains(name)) {
            return lookupStateful(className, name);
        }
        throw new IllegalArgumentException("No registered stateful or stateless beans called '" + name + "'");
    }

    @Override
    public Object invokeStateless(String name, String declaringClassName, String returnType, String methodName, String[] sig, Object[] args) throws Exception {
        Object value = statelessBeans.get(name);
        if (value == null) {
            throw new IllegalStateException("No proxy found for: " + name);
        }

        return invokeMethod(value, returnType, methodName, sig, args);
    }

    @Override
    public Object invokeStateful(String name, String declaringClassName, String returnType, String methodName, long sessionId, String[] sig, Object[] args) throws Exception {
        Object value = statefulBeanInstances.get(sessionId);
        if (value == null) {
            throw new IllegalStateException("No proxy found for: " + name);
        }
        return invokeMethod(value, returnType, methodName, sig, args);
    }

    private Object invokeMethod(Object value, String returnType, String methodName, String[] sig, Object[] args) throws Exception {
        Method m;
        try {
            m = MethodUtil.getMethod(index, value.getClass(), returnType, methodName, sig);
        } catch (Exception e) {
            throw new RuntimeException("Could not find method called " + methodName + " with signature " + Arrays.toString(sig));
        }

        return m.invoke(value, args);
    }

    @Override
    public void start() {
        log.info("Starting remote ejb invocation mbean");
    }

    public void stop() {
        statefulBeanInstances.clear();
        statelessBeans.clear();
    }

    @Override
    public void setStatelessBeanNames(String names) {
        parseNames(statelessBeanNames, names);
    }

    @Override
    public void setStatefulBeanNames(String names) {
        parseNames(statefulBeanNames, names);
    }

    private void parseNames(Set<String> set, String value){
        if (value != null && value.trim().length() > 0) {
            for (String s : value.split(",")) {
                set.add(s.trim());
            }
        }
    }

    private Object lookupStateless(String className, String name) throws NamingException {
        Object value = statelessBeans.get(name);
        if (value != null) {
            return new StatelessBeanHandler(name);
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
        if (!clazz.isAssignableFrom(value.getClass())) {
            throw new NamingException("Expected " + className + " for " + name);
        }
        statelessBeans.put(name, value);
        return new StatelessBeanHandler(name);
    }

    private Object lookupStateful(String className, String name) throws NamingException {
        InitialContext context = new InitialContext();
        Object value = context.lookup(name);
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            NamingException ex = new NamingException("Could not find class for " + className + " in loader: "  + e.getMessage());
            ex.setRootCause(e);
            throw ex;
        }
        if (!clazz.isAssignableFrom(value.getClass())) {
            throw new NamingException("Expected " + className + " for " + name);
        }
        return createStatefulHandler(name, value);
    }

    private synchronized StatefulBeanHandler createStatefulHandler(String name, Object stateful) {
        long id = (long)(Math.random() * Long.MAX_VALUE);
        while (true) {
            if (!statefulBeanInstances.containsKey(id)) {
                statefulBeanInstances.put(id, stateful);
                return new StatefulBeanHandler(name, id);
            }
            id = (long)(Math.random() * Long.MAX_VALUE);
        }
    }

}
