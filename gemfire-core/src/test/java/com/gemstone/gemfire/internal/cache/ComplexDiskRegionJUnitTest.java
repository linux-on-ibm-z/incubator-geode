/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.internal.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

import com.gemstone.gemfire.StatisticsFactory;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.internal.cache.DirectoryHolder;
import com.gemstone.gemfire.test.junit.categories.IntegrationTest;

/**
 * Unit testing for ComplexDiskRegion API's
 * 
 * @author Mitul Bid
 * 
 *  
 */
@Category(IntegrationTest.class)
public class ComplexDiskRegionJUnitTest extends DiskRegionTestingBase
{

  DiskRegionProperties diskProps = new DiskRegionProperties();

  

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    diskProps.setDiskDirs(dirs);
    DiskStoreImpl.SET_IGNORE_PREALLOCATE = true;
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    DiskStoreImpl.SET_IGNORE_PREALLOCATE = false;
  }

  /**
   * Test method for
   * 'com.gemstone.gemfire.internal.cache.ComplexDiskRegion.addToBeCompacted(Oplog)'
   * 
   * The test will test that an oplog is correctly being added to be rolled
   */
  @Test
  public void testAddToBeCompacted()
  {
    deleteFiles();
    diskProps.setRolling(false);
    diskProps.setAllowForceCompaction(true);
    region = DiskRegionHelperFactory.getSyncPersistOnlyRegion(cache, diskProps, Scope.LOCAL);
    DiskRegion dr = ((LocalRegion)region).getDiskRegion();
    StatisticsFactory factory = region.getCache().getDistributedSystem();
    Oplog oplog1 = new Oplog(11, dr.getOplogSet(), new DirectoryHolder(factory,dirs[1], 1000, 0));
    Oplog oplog2 = new Oplog(12, dr.getOplogSet(), new DirectoryHolder(factory,dirs[2], 1000, 1));
    Oplog oplog3 = new Oplog(13, dr.getOplogSet(), new DirectoryHolder(factory,dirs[3], 1000, 2));
    // give these guys some fake "live" entries
    oplog1.incTotalCount();
    oplog1.incLiveCount();
    oplog2.incTotalCount();
    oplog2.incLiveCount();
    oplog3.incTotalCount();
    oplog3.incLiveCount();

    dr.addToBeCompacted(oplog1);
    dr.addToBeCompacted(oplog2);
    dr.addToBeCompacted(oplog3);

    assertEquals(null, dr.getOplogToBeCompacted());

    oplog1.incTotalCount();
    if (oplog1 != dr.getOplogToBeCompacted()[0]) {
      fail(" expected oplog1 to be the first oplog but not the case !");
    }
    dr.removeOplog(oplog1.getOplogId());
    assertEquals(null, dr.getOplogToBeCompacted());

    oplog2.incTotalCount();
    if (oplog2 != dr.getOplogToBeCompacted()[0]) {
      fail(" expected oplog2 to be the first oplog but not the case !");
    }
    dr.removeOplog(oplog2.getOplogId());
    assertEquals(null, dr.getOplogToBeCompacted());

    oplog3.incTotalCount();
    if (oplog3 != dr.getOplogToBeCompacted()[0]) {
      fail(" expected oplog3 to be the first oplog but not the case !");
    }
    dr.removeOplog(oplog3.getOplogId());

    oplog1.destroy();
    oplog2.destroy();
    oplog3.destroy();
    closeDown();
    deleteFiles();

  }

  /**
   *  
   * Test method for
   * 'com.gemstone.gemfire.internal.cache.ComplexDiskRegion.removeFirstOplog(Oplog)'
   * 
   * The test verifies the FIFO property of the oplog set (first oplog to be added should be
   * the firs to be rolled).
   */
  @Test
  public void testRemoveFirstOplog()
  {
    deleteFiles();
    diskProps.setRolling(false);
    region = DiskRegionHelperFactory.getSyncPersistOnlyRegion(cache, diskProps, Scope.LOCAL);
    DiskRegion dr = ((LocalRegion)region).getDiskRegion();
    StatisticsFactory factory = region.getCache().getDistributedSystem();
    Oplog oplog1 = new Oplog(11, dr.getOplogSet(), new DirectoryHolder(factory,dirs[1], 1000, 0));
    Oplog oplog2 = new Oplog(12, dr.getOplogSet(), new DirectoryHolder(factory,dirs[2], 1000, 1));
    Oplog oplog3 = new Oplog(13, dr.getOplogSet(), new DirectoryHolder(factory,dirs[3], 1000, 2));
    // give these guys some fake "live" entries
    oplog1.incTotalCount();
    oplog1.incLiveCount();
    oplog2.incTotalCount();
    oplog2.incLiveCount();
    oplog3.incTotalCount();
    oplog3.incLiveCount();

    dr.addToBeCompacted(oplog1);
    dr.addToBeCompacted(oplog2);
    dr.addToBeCompacted(oplog3);

    if (oplog1 != dr.removeOplog(oplog1.getOplogId())) {
      fail(" expected oplog1 to be the first oplog but not the case !");
    }

    if (oplog2 != dr.removeOplog(oplog2.getOplogId())) {
      fail(" expected oplog2 to be the first oplog but not the case !");
    }
    if (oplog3 != dr.removeOplog(oplog3.getOplogId())) {
      fail(" expected oplog3 to be the first oplog but not the case !");
    }
    oplog1.destroy();
    oplog2.destroy();
    oplog3.destroy();

    closeDown();
    deleteFiles();
  }

}
