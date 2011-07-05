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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.management.ObjectName;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.remote.jmx.client.Client;
import org.jboss.as.remote.jmx.client.ClientFactory;
import org.jboss.as.remote.jmx.common.MethodUtil;
import org.jboss.as.remote.jmx.mbean.RemoteViaJMX;
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
 * For the jms parts of this test to work AS 7.0.0.CR1 must be started up with JMS enabled: <br/>
 * {@code ./build/target/jboss-7.0.0.CR2-SNAPSHOT/bin/standalone.sh --server-config=standalone-preview.xml}
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@RunWith(Arquillian.class)
@RunAsClient
public class EjbInEarTestCase {

    /**
     * This creates an EAR file containing an EJB jar, and a sar which contains our MBean allowing access to the server
     */
    @Deployment
    public static Archive<?> createEar() throws Exception {
        //EJB jar
        JavaArchive ejb = ShrinkWrap.create(JavaArchive.class, "test-ejb.jar");
        ejb.addPackage(TestStatelessBean.class.getPackage());

        //Sar
        JavaArchive sar = ShrinkWrap.create(JavaArchive.class, "test.sar");
        sar.addPackage(Client.class.getPackage());
        sar.addPackage(MethodUtil.class.getPackage());
        sar.addPackage(RemoteViaJMX.class.getPackage());

        URL url = EjbInEarTestCase.class.getClassLoader().getResource("sar");
        File file = new File(url.toURI());
        if (!file.exists()) {
            throw new IllegalArgumentException("Could not find " + file.getAbsolutePath());
        }
        File metaInfDir = new File(file, "META-INF");
        ArchivePath metaInf = ArchivePaths.create(ArchivePaths.create("/"), "META-INF");
        sar.add(new FileAsset(new File(metaInfDir, "jboss-service.xml")), ArchivePaths.create(metaInf, "jboss-service.xml"));
        //We need to add the extra module dependencies for the looukup of RemoteConnectionFactory
        sar.add(new FileAsset(new File(metaInfDir, "MANIFEST.MF")), ArchivePaths.create(metaInf, "MANIFEST.MF"));

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
            }

        } finally {
            client.remove();
        }
    }

    @Test
    public void testJmsLookup() throws Exception {
        Client client = ClientFactory.INSTANCE.getOrCreateClient(new ObjectName("jboss:name=test,type=remote"), "localhost", 1090);
        QueueConnection conn = null;
        QueueSession session = null;
        try {
            QueueConnectionFactory qcf = client.lookup(QueueConnectionFactory.class, "RemoteConnectionFactory");
            Queue queue = client.lookup(Queue.class, "queue/test");
            conn = qcf.createQueueConnection();
            conn.start();
            session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            final CountDownLatch latch = new CountDownLatch(10);
            final List<String> result = new ArrayList<String>();

            // Set the async listener
            QueueReceiver recv = session.createReceiver(queue);
            recv.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message message) {
                    TextMessage msg = (TextMessage)message;
                    try {
                        result.add(msg.getText());
                        latch.countDown();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });

            QueueSender sender = session.createSender(queue);
            for (int i = 0 ; i < 10 ; i++) {
                String s = "Test" + i;
                TextMessage msg = session.createTextMessage(s);
                sender.send(msg);
            }

            Assert.assertTrue(latch.await(3, TimeUnit.SECONDS));
            Assert.assertEquals(10, result.size());
            for (int i = 0 ; i < result.size() ; i++) {
                Assert.assertEquals("Test" + i, result.get(i));
            }

        } finally {
            try {
                conn.stop();
            } catch (Exception ignore) {
            }
            try {
                session.close();
            } catch (Exception ignore) {
            }
            try {
                conn.close();
            } catch (Exception ignore) {
            }
            client.remove();
        }

    }
}
