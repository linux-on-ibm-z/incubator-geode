/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.internal.cache.partitioned.fixed;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.cache.EntryOperation;
import com.gemstone.gemfire.cache.FixedPartitionResolver;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.internal.cache.xmlcache.Declarable2;

public class QuarterPartitionResolver implements FixedPartitionResolver,
    Declarable2, DataSerializable {
  private Properties resolveProps;

  public QuarterPartitionResolver() {
    this.resolveProps = new Properties();
    this.resolveProps.setProperty("routingType", "key");
  }

  int numBuckets;

  public String getPartitionName(EntryOperation opDetails,
      Set allAvailablePartitions) {
    Date date = (Date)opDetails.getKey();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH);
    if (month == 0 || month == 1 || month == 2) {
      return "Q1";
    }
    else if (month == 3 || month == 4 || month == 5) {
      return "Q2";
    }
    else if (month == 6 || month == 7 || month == 8) {
      return "Q3";
    }
    else if (month == 9 || month == 10 || month == 11) {
      return "Q4";
    }
    else {
      return "Invalid Quarter";
    }
  }

  public String getName() {
    return "QuarterPartitionResolver";
  }

  public Serializable getRoutingObject(EntryOperation opDetails) {
    Date date = (Date)opDetails.getKey();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH);
    return month;
  }

  public void close() {
    // TODO Auto-generated method stub

  }

  public void setnumBuckets(int numBukcets) {
    this.numBuckets = numBukcets;
  }

  public int getNumBuckets(String partitionName, String regionName,
      PartitionAttributes partitionAttributes) {
    return this.numBuckets;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!obj.getClass().equals(this.getClass())) {
      return false;
    }
    QuarterPartitionResolver other = (QuarterPartitionResolver)obj;
    if (!this.resolveProps.equals(other.getConfig())) {
      return false;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.gemstone.gemfire.internal.cache.xmlcache.Declarable2#getConfig()
   */
  public Properties getConfig() {
    return this.resolveProps;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.gemstone.gemfire.cache.Declarable#init(java.util.Properties)
   */
  public void init(Properties props) {
    this.resolveProps.putAll(props);
  }

  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.resolveProps = DataSerializer.readProperties(in);
    this.numBuckets = in.readInt();
  }

  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeProperties(this.resolveProps, out);
    out.writeInt(this.numBuckets);
    
  }

}
