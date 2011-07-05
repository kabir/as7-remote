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

import javax.naming.NamingException;

/**
 * Configures an MBean to allow JNDI lookups and EJB invocations from outside the JVM.
 * Include as a SAR within your EAR to use it, and grant access via the setXXXNames() methods
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public interface RemoteViaJMXMBean {
    void start();

    /**
     * A comma-separated list of JNDI names for Stateless session beans
     * that will be accessible remotely
     *
     * @param names the jndi names for stateless session beans
     */
    void setStatelessBeanNames(String names);

    /**
     * A comma-separated list of JNDI names for Stateful session beans
     * that will be accessible remotely
     *
     * @param names the jndi names for stateful session beans
     */
    void setStatefulBeanNames(String names);

    /**
     * A comma-separated list of JNDI names for general things bound in JNDI
     * which need no special processing (such as JMS ConnectionFactories and Queues)
     *
     * @param names the jndi names for stateful session beans
     */
    void setRawNames(String names);

    /**
     * Looks up something via JNDI
     *
     * @param className the expected class name, may be {@code null} for non-ejb lookups
     * @param name the jndi name
     */
    Object lookup(String className, String name) throws NamingException;

    /**
     * Invokes a method on a stateless session bean
     *
     * @param name the JNDI name of the slsb
     * @param declaringClassName the name of the interface on which the method is declared
     * @param returnType the return type of the method
     * @param methodName the name of the method
     * @param sig the array of the jvm signature of each parameter
     * @param args the actual arguments used for calling the method
     */
    Object invokeStateless(String name, String declaringClassName, String returnType, String methodName, String[] sig, Object[] args) throws Exception;

    /**
     * Invokes a method on a stateless session bean
     *
     * @param name the JNDI name of the slsb
     * @param declaringClassName the name of the interface on which the method is declared
     * @param returnType the return type of the method
     * @param methodName the name of the method
     * @param sessionId the id of the session bean. Maintained by this mbean and initialized during the call to {@link #lookup(String, String)}
     * @param sig the array of the jvm signature of each parameter
     * @param args the actual arguments used for calling the method
     */
    Object invokeStateful(String name, String declaringClassName, String returnType, String methodName, long sessionId, String[] sig, Object[] args) throws Exception;
}
