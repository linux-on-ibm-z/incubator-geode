package com.gemstone.gemfire.internal.offheap;

import com.gemstone.gemfire.internal.Sendable;
import com.gemstone.gemfire.internal.offheap.annotations.Retained;

/**
 * Instances of this interface are references to off-heap.
 * Note: this interface is part of building the odbc drivers. Any classes or interfaces it references
 * need to be added to SQLFire.xml -sqlf-odbc-java-list.
 * Because of odbc we do not want this interface to extend CachedDeserializable because it pulls in too many of the GemFire code base.
 * 
 * @author darrel
 * @since 9.0
 */
public interface OffHeapReference extends Releasable, Sendable {

  /**
   * Call to indicate that this object's memory is in use by the caller.
   * The memory will stay allocated until {@link #release()} is called.
   * It is ok for a thread other than the one that called this method to call release.
   * This method is called implicitly at the time the chunk is allocated.
   * Note: @Retained tells you that "this" is retained by this method.
   * 
   * @throws IllegalStateException if the max ref count is exceeded.
   * @return true if we are able to retain this chunk; false if we need to retry
   */
  @Retained
  public boolean retain();

  /**
   * Returns true if the value stored in this memory chunk is a serialized object. Returns false if it is a byte array.
   */
  public boolean isSerialized();

  /**
   * Returns true if the value stored in this memory chunk is compressed. Returns false if it is uncompressed.
   */
  public boolean isCompressed();

  /**
   * Returns the data stored in this object as a deserialized heap object.
   * If it is not serialized then the result will be a byte[].
   * Otherwise the deserialized heap form of the stored object is returned.
   * @return the data stored in this object as a deserialized heap object.
   */
  public Object getValueAsDeserializedHeapObject();

  /**
   * Returns the data stored in this object as a heap byte array.
   * If it is not serialized then the result will only contain the raw bytes stored in this object.
   * Otherwise the serialized heap form of the stored object is returned.
   * @return the data stored in this object as a heap byte array.
   */
  public byte[] getValueAsHeapByteArray();

}