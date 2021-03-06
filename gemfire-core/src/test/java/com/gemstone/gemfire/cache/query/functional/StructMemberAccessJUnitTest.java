/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
/*
 * StructMemberAcessJUnitTest.java
 * JUnit based test
 *
 * Created on March 24, 2005, 5:54 PM
 */
package com.gemstone.gemfire.cache.query.functional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.query.CacheUtils;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.Utils;
import com.gemstone.gemfire.cache.query.data.Address;
import com.gemstone.gemfire.cache.query.data.Employee;
import com.gemstone.gemfire.cache.query.data.Manager;
import com.gemstone.gemfire.cache.query.data.Portfolio;
import com.gemstone.gemfire.cache.query.internal.StructSet;
import com.gemstone.gemfire.cache.query.types.CollectionType;
import com.gemstone.gemfire.cache.query.types.StructType;
import com.gemstone.gemfire.test.junit.categories.IntegrationTest;

/**
 * @author vaibhav
 */
@Category(IntegrationTest.class)
public class StructMemberAccessJUnitTest {

  @Before
  public void setUp() throws java.lang.Exception {
    CacheUtils.startCache();
    Region region = CacheUtils.createRegion("Portfolios", Portfolio.class);
    for (int i = 0; i < 4; i++) {
      region.put("" + i, new Portfolio(i));
    }
  }

  @After
  public void tearDown() throws java.lang.Exception {
    CacheUtils.closeCache();
  }

  @Test
  public void testUnsupportedQueries() throws Exception {
    String queries[] = {
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos)"
            + " WHERE value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos) p"
            + " WHERE p.get(1).value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos) p"
            + " WHERE p[1].value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos) p"
            + " WHERE p.value.secId = 'IBM'"};
    for (int i = 0; i < queries.length; i++) {
      try {
        Query q = CacheUtils.getQueryService().newQuery(queries[i]);
        Object r = q.execute();
        CacheUtils.log(Utils.printResult(r));
        fail(queries[i]);
      } catch (Exception e) {
        //e.printStackTrace();
      }
    }
  }

  @Test
  public void testSupportedQueries() throws Exception {
    String queries[] = {
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos)"
            + " WHERE pos.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios AS ptf, positions AS pos)"
            + " WHERE pos.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM ptf IN /Portfolios, pos IN positions)"
            + " WHERE pos.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT pos AS myPos FROM /Portfolios ptf, positions pos)"
            + " WHERE myPos.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos) p"
            + " WHERE p.pos.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos) p"
            + " WHERE pos.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios, positions) p"
            + " WHERE p.positions.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios, positions)"
            + " WHERE positions.value.secId = 'IBM'",
        "SELECT DISTINCT * FROM"
            + " (SELECT DISTINCT * FROM /Portfolios ptf, positions pos) p"
            + " WHERE p.get('pos').value.secId = 'IBM'",
        "SELECT DISTINCT name FROM"
            + " /Portfolios , secIds name where length > 0 ",};
    for (int i = 0; i < queries.length; i++) {
      try {
        Query q = CacheUtils.getQueryService().newQuery(queries[i]);
        Object r = q.execute();
        CacheUtils.log(Utils.printResult(r));
      } catch (Exception e) {
        e.printStackTrace();
        fail(queries[i]);
      }
    }
  }

  @Test
  public void testResultComposition() throws Exception {
    String queries[] = { "select distinct p from /Portfolios p where p.ID > 0",
        "select distinct p.getID from /Portfolios p where p.ID > 0 ",
        "select distinct p.getID as secID from /Portfolios p where p.ID > 0 "};
    for (int i = 0; i < queries.length; i++) {
      Query q = CacheUtils.getQueryService().newQuery(queries[i]);
      Object o = q.execute();
      if (o instanceof SelectResults) {
        SelectResults sr = (SelectResults) o;
        if (sr instanceof StructSet && i != 2)
            fail(" StructMemberAccess::testResultComposition: Got StrcutSet when expecting ResultSet");
        CollectionType ct = sr.getCollectionType();
        CacheUtils.log("***Elememt Type of Colelction = "
            + ct.getElementType());
        CacheUtils.log((sr.getCollectionType())
            .getElementType().getSimpleClassName());
        List ls = sr.asList();
        for (int j = 0; j < ls.size(); ++j)
          CacheUtils.log("Object in the resultset = "
              + ls.get(j).getClass());
        switch (i) {
          case 0:
            if (ct.getElementType().getSimpleClassName().equals("Portfolio")) {
              assertTrue(true);
            } else {
              System.out
                  .println("StructMemberAcessJUnitTest::testResultComposition:Colelction Element's class="
                      + ct.getElementType().getSimpleClassName());
              fail();
            }
            break;
          case 1:
            if (ct.getElementType().getSimpleClassName().equals("int")) {
              assertTrue(true);
            } else {
              System.out
                  .println("StructMemberAcessJUnitTest::testResultComposition:Colelction Element's class="
                      + ct.getElementType().getSimpleClassName());
              fail();
            }
            break;
          case 2:
            if (ct.getElementType().getSimpleClassName().equals("Struct")) {
              assertTrue(true);
            } else {
              System.out
                  .println("StructMemberAcessJUnitTest::testResultComposition:Colelction Element's class="
                      + ct.getElementType().getSimpleClassName());
              fail();
            }
        }
      }
    }
  }

  public void _BUGtestSubClassQuery() throws Exception {
    Set add1 = new HashSet();
    Set add2 = new HashSet();
    add1.add(new Address("Hp3 9yf", "Apsley"));
    add1.add(new Address("Hp4 9yf", "Apsleyss"));
    add2.add(new Address("Hp3 8DZ", "Hemel"));
    add2.add(new Address("Hp4 8DZ", "Hemel"));
    Region region = CacheUtils.createRegion("employees", Employee.class);
    region.put("1", new Manager("aaa", 27, 270, "QA", 1800, add1, 2701));
    region.put("2", new Manager("bbb", 28, 280, "QA", 1900, add2, 2801));
    String queries[] = { "SELECT DISTINCT e.manager_id FROM /employees e"};
    for (int i = 0; i < queries.length; i++) {
      Query q = CacheUtils.getQueryService().newQuery(queries[i]);
      Object r = q.execute();
      CacheUtils.log(Utils.printResult(r));
      String className = (((SelectResults) r).getCollectionType())
          .getElementType().getSimpleClassName();
      if (className.equals("Employee")) {
        CacheUtils.log("pass");
      } else {
        fail("StructMemberAccessTest::testSubClassQuery:failed .Expected class name Employee. Actualy obtained="
            + className);
      }
    }
  }

  @Test
  public void testBugNumber_32354() {
    String queries[] = { "select distinct * from /root/portfolios.values, positions.values ",};
    int i = 0;
    try {
      tearDown();
      CacheUtils.startCache();
      Region rootRegion = CacheUtils.createRegion("root", null);
      AttributesFactory attributesFactory = new AttributesFactory();
      attributesFactory.setValueConstraint(Portfolio.class);
      RegionAttributes regionAttributes = attributesFactory
          .create();
      Region region = rootRegion
          .createSubregion("portfolios", regionAttributes);
      for (i = 0; i < 4; i++) {
        region.put("" + i, new Portfolio(i));
      }
      for (i = 0; i < queries.length; i++) {
        Query q = CacheUtils.getQueryService().newQuery(queries[i]);
        Object r = q.execute();
        CacheUtils.log(Utils.printResult(r));
        StructType type = ((StructType) ((SelectResults) r).getCollectionType()
            .getElementType());
        String fieldNames[] = type.getFieldNames();
        for (i = 0; i < fieldNames.length; ++i) {
          String name = fieldNames[i];
          CacheUtils.log("Struct Field name = " + name);
          if (name.equals("/root/portfolios")
              || name.equals("positions.values")) {
            fail("The field name in struct = " + name);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(queries[i]);
    }
  }

  @Test
  public void testBugNumber_32355() {
    String queries[] = { "select distinct positions.values.toArray[0], positions.values.toArray[0],status from /Portfolios",};
    int i = 0;
    try {
      for (i = 0; i < queries.length; i++) {
        Query q = CacheUtils.getQueryService().newQuery(queries[i]);
        Object r = q.execute();
        CacheUtils.log(Utils.printResult(r));
        StructType type = ((StructType) ((SelectResults) r).getCollectionType()
            .getElementType());
        String fieldNames[] = type.getFieldNames();
        for (i = 0; i < fieldNames.length; ++i) {
          String name = fieldNames[i];
          CacheUtils.log("Struct Field name = " + name);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(queries[i]);
    }
  }
}
