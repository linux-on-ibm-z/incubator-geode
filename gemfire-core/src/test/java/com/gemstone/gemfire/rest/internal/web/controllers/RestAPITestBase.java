package com.gemstone.gemfire.rest.internal.web.controllers;

import java.util.Properties;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheClosedException;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.internal.AvailablePortHelper;

import dunit.DistributedTestCase;
import dunit.Host;
import dunit.VM;

public class RestAPITestBase extends DistributedTestCase {
  private static final long serialVersionUID = 1L;
  public static Cache cache = null;
  VM vm0 = null;
  VM vm1 = null;
  VM vm2 = null;
  VM vm3 = null;
  
  public RestAPITestBase(String name) {
    super(name);
  }
  
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    disconnectAllFromDS();
    pause(5000);
    final Host host = Host.getHost(0);
    vm0 = host.getVM(0);
    vm1 = host.getVM(1);
    vm2 = host.getVM(2);
    vm3 = host.getVM(3);
  }  
  
  /**
   * close the clients and teh servers
   */
  @Override
  public void tearDown2() throws Exception
  {
    vm0.invoke(getClass(), "closeCache");
    vm1.invoke(getClass(), "closeCache");
    vm2.invoke(getClass(), "closeCache");
    vm3.invoke(getClass(), "closeCache");
  }

  /**
   * close the cache
   * 
   */
  public static void closeCache() {
    if (cache != null && !cache.isClosed()) {
      cache.close();
      cache.getDistributedSystem().disconnect();
    }
  }
  
  protected static String createCache(VM currentVM) {
    
    RestAPITestBase test = new RestAPITestBase(testName);
    
    final String hostName = currentVM.getHost().getHostName();
    final int serverPort = AvailablePortHelper.getRandomAvailableTCPPort();
    
    Properties props = new Properties();
    
    props.setProperty(DistributionConfig.START_DEV_REST_API_NAME, "true");
    props.setProperty(DistributionConfig.HTTP_SERVICE_BIND_ADDRESS_NAME, hostName);
    props.setProperty(DistributionConfig.HTTP_SERVICE_PORT_NAME,String.valueOf(serverPort));
    

    InternalDistributedSystem ds = test.getSystem(props);
    cache = CacheFactory.create(ds);
    return "http://" + hostName + ":" + serverPort + "/gemfire-api/v1";
    
  }
  
  public static String createCacheWithGroups (VM vm, final String groups, final String regionName ) {
    RestAPITestBase test = new RestAPITestBase(testName);
    
    final String hostName = vm.getHost().getHostName(); 
    final int serverPort = AvailablePortHelper.getRandomAvailableTCPPort();
    
    Properties props = new Properties();
    
    if(groups != null) {
      props.put("groups", groups);
    }
    
    props.setProperty(DistributionConfig.START_DEV_REST_API_NAME, "true");
    props.setProperty(DistributionConfig.HTTP_SERVICE_BIND_ADDRESS_NAME, hostName);
    props.setProperty(DistributionConfig.HTTP_SERVICE_PORT_NAME, String.valueOf(serverPort));
    
    InternalDistributedSystem ds = test.getSystem(props);
    cache = CacheFactory.create(ds);
    
    String restEndPoint =  "http://" + hostName + ":" + serverPort + "/gemfire-api/v1";
    return restEndPoint; 
  }
  
}
