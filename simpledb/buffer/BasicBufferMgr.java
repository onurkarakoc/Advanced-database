package simpledb.buffer;

import simpledb.file.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
   // Treat as queue with enqueue() and dequeue() functions.
   private ArrayList<Buffer> unpinnedBufferQueue;
   private Map<Block, Buffer> allocatedBuffersMap;

   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   BasicBufferMgr(int numbuffs) {
      unpinnedBufferQueue = new ArrayList<>();
      allocatedBuffersMap = new HashMap<>();
      for (int i=0; i<numbuffs; i++)
         enqueue(new Buffer(i));
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
      for (Buffer buffer : unpinnedBufferQueue)
         if (buffer.isModifiedBy(txnum))
            buffer.flush();
      for (Buffer buffer : allocatedBuffersMap.values())
         if (buffer.isModifiedBy(txnum))
            buffer.flush();
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the queue is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) {
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null)
            return null;
         buff.assignToBlock(blk);
         // If map contains the buffer with different block, change it with new one.
         if (allocatedBuffersMap.containsValue(buff)) {
            Block blockWillChange = null;
            for (Map.Entry<Block, Buffer> entry : allocatedBuffersMap.entrySet()) {
               if (entry.getValue().equals(buff)) {
                  blockWillChange = entry.getKey();
               }
            }
            allocatedBuffersMap.remove(blockWillChange);
         }
         allocatedBuffersMap.put(blk, buff);
      }
      // If existing buffer is not pinned yet, remove it from the queue and make it pinned.
      if (!buff.isPinned()) {
         unpinnedBufferQueue.remove(buff);
      }
      buff.pin();
      return buff;
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null)
         return null;
      buff.assignToNew(filename, fmtr);
      buff.pin();
      allocatedBuffersMap.put(buff.block(), buff);
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
      buff.unpin();
      if (!buff.isPinned())
         enqueue(buff);
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers.
    */
   int available() {
      return unpinnedBufferQueue.size();
   }

   /**
    * @param blk
    * @return the buffer, if allocatedBuffersMap contains that block
    * otherwise return null value.
    */
   private Buffer findExistingBuffer(Block blk) {
     if (allocatedBuffersMap.containsKey(blk))
        return allocatedBuffersMap.get(blk);
     else
        return null;
   }

   /**
    * If there are any available buffer in the queue:
    * @return the first buffer otherwise return null.
    */
   private Buffer chooseUnpinnedBuffer() {
      if (available() > 0)
        return dequeue();
      return null;
   }

   /**
    * Adds a buffer at the end of the list.
    * @param buffer
    */
   private void enqueue(Buffer buffer) {
      if (!unpinnedBufferQueue.contains(buffer))
         unpinnedBufferQueue.add(buffer);
   }

   /**
    * @return a buffer from the beginning of the list and removes it from the list.
    */
   private Buffer dequeue() {
      return unpinnedBufferQueue.remove(0);
   }

   /**
    * We'll use this in BufferMgr for printStatus function.
    * @return allocatedBuffersMap.
    */
   public Map<Block, Buffer> getAllocatedBuffersMap() {
      return allocatedBuffersMap;
   }

   /**
    * We'll use this in BufferMgr for printStatus function.
    * @return unpinnedBufferQueue.
    */
   public ArrayList<Buffer> getUnpinnedBufferQueue() {
      return unpinnedBufferQueue;
   }
}
