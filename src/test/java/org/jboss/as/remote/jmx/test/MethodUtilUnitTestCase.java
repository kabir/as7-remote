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

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.jboss.as.remote.jmx.common.MethodUtil;
import org.jboss.as.remote.jmx.mbean.DeploymentReflectionIndex;
import org.junit.Test;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class MethodUtilUnitTestCase {

    DeploymentReflectionIndex index = DeploymentReflectionIndex.create();

    @Test
    public void testNoArgsMethod() throws Exception {
        doTest("noArgsMethod");
    }

    @Test
    public void testSimpleMethod() throws Exception {
        doTest("simpleMethod");
    }

    @Test
    public void testPrimitivesMethod() throws Exception {
        doTest("primitivesMethod");
    }

    @Test
    public void testPrimitiveWrappersMethod() throws Exception {
        doTest("primitiveWrappersMethod");
    }

    @Test
    public void testArrayMethod() throws Exception {
        doTest("arrayMethod");
    }

    @Test
    public void testArrayPrimitivesMethod() throws Exception {
        doTest("arrayPrimitivesMethod");
    }

    @Test
    public void testArrayPrimitiveWrappersMethod() throws Exception {
        doTest("arrayPrimitiveWrappersMethod");
    }

    public void noArgsMethod() {
    }

    public void simpleMethod(String s){
    }

    public void primitivesMethod(int i, boolean bool, byte b, short s, double d, long l, float f) {
    }

    public void primitiveWrappersMethod(Integer i, Boolean bool, Byte b, Short s, Double d, Long l, Float f) {
    }

    public void arrayMethod(String[] s){
    }

    public void arrayPrimitivesMethod(int[] i, boolean[][] bool, byte[] b, short[] s, double[] d, long[] l, float[] f) {
    }

    public void arrayPrimitiveWrappersMethod(Integer[] i, Boolean[][] bool, Byte[] b, Short[] s, Double[] d, Long[] l, Float[] f) {
    }

    private void doTest(String name) throws Exception {
        Method original = null;
        for (Method m : this.getClass().getMethods()) {
            if (m.getName().equals(name)) {
                original = m;
                break;
            }
        }
        Assert.assertNotNull(original);
        String[] sig = MethodUtil.getSignature(original);
        Method found = MethodUtil.getMethod(index, this.getClass(), Void.TYPE.getName(), name, sig);
        //Method found = MethodUtil.getMethod(this.getClass(), name, sig);
        Assert.assertEquals(original, found);
    }
}
