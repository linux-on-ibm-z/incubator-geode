/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.internal.cache.execute;

import java.io.Serializable;
import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;

public class TestFunction extends FunctionAdapter {

  public static final String TEST_FUNCTION1 = "TestFunction1";

  public static final String TEST_FUNCTION2 = "TestFunction2";

  public static final String TEST_FUNCTION3 = "TestFunction3";

  public static final String TEST_FUNCTION4 = "TestFunction4";

  public static final String TEST_FUNCTION5 = "TestFunction5";

  private final Properties props;

  private static final String ID = "id";

  private static final String HAVE_RESULTS = "haveResults";

  public TestFunction() {
    super();
    this.props = new Properties();
  }

  public Properties getProps() {
    return props;
  }

  public TestFunction(boolean haveResults, String id) {
    this.props = new Properties();
    this.props.setProperty(HAVE_RESULTS, Boolean.toString(haveResults));
    this.props.setProperty(ID, id);
  }

  public void execute(FunctionContext context) {
    String id = this.props.getProperty(ID);

    if (id.equals(TEST_FUNCTION1)) {
      execute1(context);
    }
    else if (id.equals(TEST_FUNCTION2)) {
      execute2(context);
    }
    else if (id.equals(TEST_FUNCTION3)) {
      execute2(context);
    }
    else if (id.equals(TEST_FUNCTION4)) {
      execute2(context);
    }
    else if (id.equals(TEST_FUNCTION5)) {
      execute5(context);
    }
  }

  public void execute1(FunctionContext context) {
    DistributedSystem ds = InternalDistributedSystem.getAnyInstance();
    LogWriter logger = ds.getLogWriter();
    logger.info("Executing executeException in TestFunction on Member : "
        + ds.getDistributedMember() + "with Context : " + context);
    context.getResultSender().lastResult((Serializable) context.getArguments());
  }

  public void execute2(FunctionContext context) {
    DistributedSystem ds = InternalDistributedSystem.getAnyInstance();
    LogWriter logger = ds.getLogWriter();
    try {
      synchronized (this) {
        this.wait(20000000);
      }
    }
    catch (InterruptedException e) {

    }
    context.getResultSender().lastResult(Boolean.TRUE);
  }

  public void execute5(FunctionContext context) {
    DistributedSystem ds = InternalDistributedSystem.getAnyInstance();
    LogWriter logger = ds.getLogWriter();

    if (this.props.get("TERMINATE") != null
        && this.props.get("TERMINATE").equals("YES")) {
      logger.info("Function Terminated");
    }
    else {
      try {
        synchronized (this) {
          logger.info("Function Running");
          this.wait(20000);
        }
      }
      catch (InterruptedException e) {

      }
    }
    context.getResultSender().lastResult(Boolean.TRUE);
  }

  public String getId() {
    return this.props.getProperty(ID);
  }

  public boolean hasResult() {
    return Boolean.valueOf(this.props.getProperty(HAVE_RESULTS)).booleanValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.gemstone.gemfire.internal.cache.xmlcache.Declarable2#getConfig()
   */
  public Properties getConfig() {
    return this.props;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.gemstone.gemfire.cache.Declarable#init(java.util.Properties)
   */
  public void init(Properties props) {
    this.props.putAll(props);
  }
}
