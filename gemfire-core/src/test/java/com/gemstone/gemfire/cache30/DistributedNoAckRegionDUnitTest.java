/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.cache30;

import java.util.*;
import com.gemstone.gemfire.cache.*;

import dunit.*;

/**
 * This class tests the functionality of a cache {@link Region region}
 * that has a scope of {@link Scope#DISTRIBUTED_NO_ACK distributed no
 * ACK}.
 *
 * @author David Whitlock
 * @since 3.0
 */
public class DistributedNoAckRegionDUnitTest
  extends MultiVMRegionTestCase {

  public DistributedNoAckRegionDUnitTest(String name) {
    super(name);
  }

  /**
   * Returns region attributes for a <code>GLOBAL</code> region
   */
  protected RegionAttributes getRegionAttributes() {
    AttributesFactory factory = new AttributesFactory();
    factory.setScope(Scope.DISTRIBUTED_NO_ACK);
    factory.setDataPolicy(DataPolicy.PRELOADED);
    factory.setConcurrencyChecksEnabled(false);
    return factory.create();
  }

  //////////////////////  Test Methods  //////////////////////

  /** Tests creating a distributed subregion of a local scope region,
   * which should fail.
   */
  public void testDistSubregionOfLocalRegion() throws CacheException {
    // creating a distributed subregion of a LOCAL region is illegal.
    AttributesFactory factory = new AttributesFactory();
    factory.setScope(Scope.LOCAL);
    createRootRegion(factory.create());
    try {
      createRegion(getUniqueName());
      fail("Should have thrown an IllegalStateException");
    }
    catch (IllegalStateException e) {
      // pass
    }
  }

  /**
   * Tests the compatibility of creating certain kinds of subregions
   * of a local region.
   *
   * @see Region#createSubregion
   */
  public void testIncompatibleSubregions()
    throws CacheException, InterruptedException {
    Host host = Host.getHost(0);
    VM vm0 = host.getVM(0);
    VM vm1 = host.getVM(1);


    // Scope.GLOBAL is illegal if there is any other cache in the
    // distributed system that has the same region with
    // Scope.DISTRIBUTED_NO_ACK.

    final String name = this.getUniqueName() + "-NO_ACK";
    vm0.invoke(new SerializableRunnable("Create NO ACK Region") {
        public void run() {
          try {
            Region region = createRegion(name, "INCOMPATIBLE_ROOT", getRegionAttributes());
            assertTrue(getRootRegion("INCOMPATIBLE_ROOT").getAttributes().getScope().isDistributedNoAck());
            assertTrue(region.getAttributes().getScope().isDistributedNoAck());
          } catch (CacheException ex) {
            fail("While creating NO ACK region", ex);
          }
        }
      });

    vm1.invoke(new SerializableRunnable("Create GLOBAL Region") {
        public void run() {
          try {
            AttributesFactory factory =
              new AttributesFactory(getRegionAttributes());
            factory.setScope(Scope.GLOBAL);
            assertNull(getRootRegion("INCOMPATIBLE_ROOT"));
            try {
              createRootRegion( "INCOMPATIBLE_ROOT", factory.create());
              fail("Should have thrown an IllegalStateException");
//              createRegion(name, factory.create());
            } catch (IllegalStateException ex) {
              // pass...
            }
//            assertNull(getRootRegion());

          } catch (CacheException ex) {
            fail("While creating GLOBAL Region", ex);
          }
        }
      });
    vm1.invoke(new SerializableRunnable("Create ACK Region") {
        public void run() {
          try {
            AttributesFactory factory =
              new AttributesFactory(getRegionAttributes());
            factory.setScope(Scope.DISTRIBUTED_ACK);
            assertNull(getRootRegion("INCOMPATIBLE_ROOT"));
            try {
              createRootRegion( "INCOMPATIBLE_ROOT", factory.create());
              fail("Should have thrown an IllegalStateException");
//              createRegion(name, factory.create());
            } catch (IllegalStateException ex) {
              // pass...
            }
//            assertNull(getRootRegion());

          } catch (CacheException ex) {
            fail("While creating ACK Region", ex);
          }
        }
      });
  }

  private static final int CHUNK_SIZE = 500 * 1024; // == InitialImageOperation.CHUNK_SIZE_IN_BYTES
  // use sizes so it completes in < 15 sec, but hangs if bug exists
  private static final int NUM_ENTRIES_VM = 15000;
  private static final int VALUE_SIZE_VM = CHUNK_SIZE * 150 / NUM_ENTRIES_VM;
  
  private static final int NUM_PUTS = 100000;

  protected static volatile boolean stopPutting = false;

  /**
   * Messages pile up in overflow queue during long GetInitialImage
   * This test was disabled since we not longer have an overflow queue
   * and GII is now non-blocking (bug 30705 was caused blocking gii).
   * This test can take a long time to run on disk regions.
   */
  public void disabled_testBug30705() throws InterruptedException {    
    final String name = this.getUniqueName();
    final int numEntries = NUM_ENTRIES_VM;
    final int valueSize = VALUE_SIZE_VM;

    Host host = Host.getHost(0);
    VM vm0 = host.getVM(0);
    VM vm2 = host.getVM(2);

    SerializableRunnable create = new
      CacheSerializableRunnable("Create Mirrored Region") {
        public void run2() throws CacheException {
          getLogWriter().info("testBug30705: Start creating Mirrored Region"); 
          AttributesFactory factory =
            new AttributesFactory(getRegionAttributes());
          factory.setDataPolicy(DataPolicy.REPLICATE);
          createRegion(name, factory.create());
          getLogWriter().info("testBug30705: Finished creating Mirrored Region"); 
        }
      };
      
    SerializableRunnable put = new
      CacheSerializableRunnable("Distributed NoAck Puts") {
        public void run2() throws CacheException {
          Region rgn = getCache().getRegion("/root/" + name);
          assertNotNull(rgn);
          Object key = new Integer(0x42);
          Object value = new byte[0];
          assertNotNull(value);
          getLogWriter().info("testBug30705: Started Distributed NoAck Puts"); 
          for (int i = 0; i < NUM_PUTS; i++) {
            if (stopPutting) {
              getLogWriter().info("testBug30705: Interrupted Distributed Ack Puts after " + i + " PUTS"); 
              break;
            }
            if ((i % 1000) == 0) {
              getLogWriter().info("testBug30705: modification #" + i); 
            }
            rgn.put(key, value);
          }          
        }
    };


    vm0.invoke(create);

    vm0.invoke(new CacheSerializableRunnable("Put data") {
        public void run2() throws CacheException {
          getLogWriter().info("testBug30705: starting initial data load"); 
          Region region =
            getRootRegion().getSubregion(name);
          final byte[] value = new byte[valueSize];
          Arrays.fill(value, (byte)0x42);
          for (int i = 0; i < numEntries; i++) {
            if ((i % 1000) == 0) {
              getLogWriter().info("testBug30705: initial put #" + i); 
            }
            region.put(new Integer(i), value);
          }
          getLogWriter().info("testBug30705: finished initial data load"); 
        }
      });

    // start putting
    AsyncInvocation async = vm0.invokeAsync(put);
    
    // do initial image
    try {
      getLogWriter().info("testBug30705: before the critical create");
      vm2.invoke(create);
      getLogWriter().info("testBug30705: after the critical create");
   } finally {
      // test passes if this does not hang
      getLogWriter().info("testBug30705: INTERRUPTING Distributed NoAck Puts after GetInitialImage");
      vm0.invoke(new SerializableRunnable("Interrupt Puts") {
        public void run() {
          getLogWriter().info("testBug30705: interrupting putter"); 
          stopPutting = true;
        }
      });
      DistributedTestCase.join(async, 30 * 1000, getLogWriter());
      // wait for overflow queue to quiesce before continuing
      vm2.invoke(new SerializableRunnable("Wait for Overflow Queue") {
        public void run() {
          WaitCriterion ev = new WaitCriterion() {
            public boolean done() {
              return getSystem().getDistributionManager().getStats().getOverflowQueueSize() == 0;
            }
            public String description() {
              return "overflow queue remains nonempty";
            }
          };
          DistributedTestCase.waitForCriterion(ev, 30 * 1000, 200, true);
//          pause(100);
//           try {
//             getRootRegion().getSubregion(name).destroyRegion();
//           } catch (OperationAbortedException ignore) {
//           }
//           closeCache();
        }
       });
    } // finally
   getLogWriter().info("testBug30705: at end of test");
   if (async.exceptionOccurred()) {
     fail("Got exception", async.getException());
   }
  }

  protected void pauseIfNecessary(int ms) {
    pause(ms);
  }
  
  protected void pauseIfNecessary() {
    pause();
  }

  /**
   * The number of milliseconds to try repeating validation code in the
   * event that AssertionFailedError is thrown.  For DISTRIBUTED_NO_ACK 
   * scopes, a repeat timeout is used to account for the fact that a
   * previous operation may have not yet completed.
   */
  protected long getRepeatTimeoutMs() {
    return 5000;
  }
  
}
