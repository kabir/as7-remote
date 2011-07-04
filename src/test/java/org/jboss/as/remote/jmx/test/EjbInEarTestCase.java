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
package org.jboss.as.remote.jmx.test;

import java.io.File;
import java.net.URL;

import javax.management.ObjectName;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.remote.jmx.client.Client;
import org.jboss.as.remote.jmx.client.ClientFactory;
import org.jboss.as.remote.jmx.common.MethodUtil;
import org.jboss.as.remote.jmx.mbean.RemoteEjb;
import org.jboss.as.remote.jmx.test.ejb.TestStateful;
import org.jboss.as.remote.jmx.test.ejb.TestStateless;
import org.jboss.as.remote.jmx.test.ejb.TestStatelessBean;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@RunWith(Arquillian.class)
@RunAsClient
public class EjbInEarTestCase {

    @Deployment
    public static Archive<?> createEar() throws Exception {
        //EJB jar
        JavaArchive ejb = ShrinkWrap.create(JavaArchive.class, "test-ejb.jar");
        ejb.addPackage(TestStatelessBean.class.getPackage());

        //Sar
        JavaArchive sar = ShrinkWrap.create(JavaArchive.class, "test.sar");
        sar.addPackage(Client.class.getPackage());
        sar.addPackage(MethodUtil.class.getPackage());
        sar.addPackage(RemoteEjb.class.getPackage());

        URL url = EjbInEarTestCase.class.getClassLoader().getResource("sar");
        File file = new File(url.toURI());
        if (!file.exists()) {
            throw new IllegalArgumentException("Could not find " + file.getAbsolutePath());
        }
        File metaInfDir = new File(file, "META-INF");
        ArchivePath metaInf = ArchivePaths.create(ArchivePaths.create("/"), "META-INF");
        sar.add(new FileAsset(new File(metaInfDir, "jboss-service.xml")), ArchivePaths.create(metaInf, "jboss-service.xml"));

        //Ear
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear");
        ear.addAsModule(ejb);
        ear.addAsModule(sar);

        return ear;
    }

    @Test
    public void testNotVisibleBean() throws Exception {
        Client client = ClientFactory.INSTANCE.getOrCreateClient(new ObjectName("jboss:name=test,type=remote"), "localhost", 1090);
        try {
            client.lookup(TestStateless.class, "java:global/test/test-ejb/NotVisibleBean");
            Assert.fail("Should have had an exception");
        } catch (Exception ignore) {
        } finally {
            client.remove();
        }
    }

    @Test
    public void testSimpleStateless() throws Exception {
        Client client = ClientFactory.INSTANCE.getOrCreateClient(new ObjectName("jboss:name=test,type=remote"), "localhost", 1090);
        try {
            TestStateless bean = client.lookup(TestStateless.class, "java:global/test/test-ejb/TestStatelessBean");
            Assert.assertNotNull(bean);
            Assert.assertEquals(1, bean.test(true));
            Assert.assertEquals(0, bean.test(false));

            bean = client.lookup(TestStateless.class, "java:global/test/test-ejb/TestStatelessBean");
            Assert.assertNotNull(bean);
            Assert.assertEquals(0, bean.test(false));
            Assert.assertEquals(1, bean.test(true));
        } finally {
            client.remove();
        }
    }

    @Test
    public void testSimpleStateful() throws Exception {
        Client client = ClientFactory.INSTANCE.getOrCreateClient(new ObjectName("jboss:name=test,type=remote"), "localhost", 1090);
        try {
            TestStateful bean1 = client.lookup(TestStateful.class, "java:global/test/test-ejb/TestStatefulBean");
            bean1.setValue(100);
            Assert.assertEquals(100, bean1.getValue());

            TestStateful bean2 = client.lookup(TestStateful.class, "java:global/test/test-ejb/TestStatefulBean");
            bean2.setValue(200);
            Assert.assertEquals(200, bean2.getValue());
            Assert.assertEquals(100, bean1.getValue());

            bean1.setValue(300);
            Assert.assertEquals(300, bean1.getValue());
            Assert.assertEquals(200, bean2.getValue());

            bean1.clear();
            try {
                bean1.getValue();
                Assert.fail("Should not have been able to read value of @Removed bean");
            } catch(Exception expected) {
                expected.printStackTrace();
            }

        } finally {
            client.remove();
        }
    }
}
