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
package org.jboss.as.remote.jmx.common;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.remote.jmx.mbean.ClassReflectionIndex;
import org.jboss.as.remote.jmx.mbean.DeploymentReflectionIndex;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class MethodUtil {

    private static final Map<String, Class<?>> PRIMITIVES;
    static {
        Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();
        primitives.put("int", Integer.TYPE);
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("float", Float.TYPE);
        PRIMITIVES = Collections.unmodifiableMap(primitives);
    }


    public static String[] getSignature(Method method) {
        Class<?>[] params = method.getParameterTypes();
        String[] sig = new String[params.length];
        for (int i = 0 ; i < params.length ; i++) {
            sig[i] = params[i].getName();
        }
        return sig;
    }

    public static Method getMethod(DeploymentReflectionIndex index, Class<?> clazz, String returnType, String name, String[] sig)  throws ClassNotFoundException, NoSuchMethodException {
        System.out.println(Arrays.toString(sig));

        ClassReflectionIndex<?> classIndex = index.getClassIndex(clazz);
        return classIndex.getMethod(returnType, name, sig);
        //return clazz.getMethod(name, getArgs(clazz, sig));
    }

    private static Class<?>[] getArgs(Class<?> clazz, String[] sig) throws ClassNotFoundException {
        Class<?>[] args = new Class[sig.length];
        for (int i = 0 ; i < args.length ; i++) {
            args[i] = getClass(clazz, sig[i]);
        }
        return args;
    }

    private static Class<?> getClass(Class<?> clazz, String sig) throws ClassNotFoundException {
        if (sig.startsWith("[")) {
            return Class.forName(sig, true, clazz.getClassLoader());
        } else {
            Class<?> found = PRIMITIVES.get(sig);
            if (found != null) {
                return found;
            }
            return clazz.getClassLoader().loadClass(sig);
        }
    }
}
