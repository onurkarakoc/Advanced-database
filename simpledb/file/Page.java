package simpledb.file;

import simpledb.server.SimpleDB;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * The contents of a disk block in memory.
 * A page is treated as an array of BLOCK_SIZE bytes.
 * There are methods to get/set values into this array,
 * and to read/write the contents of this array to a disk block.
 * 
 * For an example of how to use Page and 
 * {@link Block} objects, 
 * consider the following code fragment.  
 * The first portion increments the integer at offset 792 of block 6 of file junk.  
 * The second portion stores the string "hello" at offset 20 of a page, 
 * and then appends it to a new block of the file.  
 * It then reads that block into another page 
 * and extracts the value "hello" into variable s.
 * <pre>
 * Page p1 = new Page();
 * Block blk = new Block("junk", 6);
 * p1.read(blk);
 * int n = p1.getInt(792);
 * p1.setInt(792, n+1);
 * p1.write(blk);
 *
 * Page p2 = new Page();
 * p2.setString(20, "hello");
 * blk = p2.append("junk");
 * Page p3 = new Page();
 * p3.read(blk);
 * String s = p3.getString(20);
 * </pre>
 * @author Edward Sciore
 */
public class Page {
   /**
    * The number of bytes in a block.
    * This value is set unreasonably low, so that it is easier
    * to create and test databases having a lot of blocks.
    * A more realistic value would be 4K.
    */
   public static final int BLOCK_SIZE = 400;
   
   /**
    * The size of an integer in bytes.
    * This value is almost certainly 4, but it is
    * a good idea to encode this value as a constant. 
    */
   public static final int INT_SIZE = Integer.SIZE / Byte.SIZE;
   
   /**
    * The maximum size, in bytes, of a string of length n.
    * A string is represented as the encoding of its characters,
    * preceded by an integer denoting the number of bytes in this encoding.
    * If the JVM uses the US-ASCII encoding, then each char
    * is stored in one byte, so a string of n characters
    * has a size of 4+n bytes.
    * @param n the size of the string
    * @return the maximum number of bytes required to store a string of size n
    */
   public static final int STR_SIZE(int n) {
      float bytesPerChar = Charset.defaultCharset().newEncoder().maxBytesPerChar();
      return INT_SIZE + (n * (int)bytesPerChar);
   }

   // Just 1 byte
   public static final int BOOLEAN_SIZE = Byte.BYTES;
   public static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;
   public static final int DOUBLE_SIZE = Double.SIZE / Byte.SIZE;
   // Date objects are represented by long integers
   public static final int DATE_SIZE = Long.SIZE / Byte.SIZE;
   public static final int LONG_SIZE = Long.SIZE / Byte.SIZE;

   private ByteBuffer contents = ByteBuffer.allocateDirect(BLOCK_SIZE);
   private FileMgr filemgr = SimpleDB.fileMgr();
   
   /**
    * Creates a new page.  Although the constructor takes no arguments,
    * it depends on a {@link FileMgr} object that it gets from the
    * method {@link simpledb.server.SimpleDB#fileMgr()}.
    * That object is created during system initialization.
    * Thus this constructor cannot be called until either
    * {@link simpledb.server.SimpleDB#init(String)} or
    * {@link simpledb.server.SimpleDB#initFileMgr(String)} or
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * {@link simpledb.server.SimpleDB#initFileLogAndBufferMgr(String)}
    * is called first.
    */
   public Page() {}
   
   /**
    * Populates the page with the contents of the specified disk block. 
    * @param blk a reference to a disk block
    */
   public synchronized void read(Block blk) {
      filemgr.read(blk, contents);
   }
   
   /**
    * Writes the contents of the page to the specified disk block.
    * @param blk a reference to a disk block
    */
   public synchronized void write(Block blk) {
      filemgr.write(blk, contents);
   }
   
   /**
    * Appends the contents of the page to the specified file.
    * @param filename the name of the file
    * @return the reference to the newly-created disk block
    */
   public synchronized Block append(String filename) {
      return filemgr.append(filename, contents);
   }
   
   /**
    * Returns the integer value at a specified offset of the page.
    * If an integer was not stored at that location, 
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the integer value at that offset
    */
   public synchronized int getInt(int offset) {
      contents.position(offset);
      return contents.getInt();
   }
   
   /**
    * Writes an integer to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the integer to be written to the page
    */
   public synchronized void setInt(int offset, int val) {
      if (offset >= 0 && offset + INT_SIZE <= 400) {
         contents.position(offset);
         contents.putInt(val);
      }
      else
         throw new IllegalArgumentException("Given integer does not fit to page. Please check it again. (Limit: " + BLOCK_SIZE + ")");
   }
   
   /**
    * Returns the string value at the specified offset of the page.
    * If a string was not stored at that location,
    * the behavior of the method is unpredictable.
    * @param offset the byte offset within the page
    * @return the string value at that offset
    */
   public synchronized String getString(int offset) {
      contents.position(offset);
      int len = contents.getInt();
      byte[] byteval = new byte[len];
      contents.get(byteval);
      return new String(byteval);
   }
   
   /**
    * Writes a string to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the string to be written to the page
    */
   public synchronized void setString(int offset, String val) {
      // We assumed that string of n characters has n + 4 bytes because of Java virtual machine. The alternative solution for general approach can be following:
      // if (offset >= 0 && offset & STR_SIZE(val.length() + 4 <= 400)
      if (offset >= 0 && offset + val.length() + 4 <= 400) {
         contents.position(offset);
         byte[] byteval = val.getBytes();
         contents.putInt(byteval.length);
         contents.put(byteval);
      }
      else
         throw new IllegalArgumentException("Given string does not fit to page. Please check it again. (Limit: " + BLOCK_SIZE + ")");
   }

   /**
    * Return the boolean value at the specified offset value of the page.
    * If a boolean does not stored at that location,
    * the behavior of method is unpredictable.
    * @param offset the byte offset within the page
    * @return the boolean value at that offset.
    */
   public synchronized boolean getBoolean(int offset) {
      contents.position(offset);
      // There are just two options : 1 and 0
      return contents.get() == 1;
   }

   /**
    * Writes a boolean to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the boolean to be written to the page
    */
   public synchronized void setBoolean(int offset, boolean val) {
      if (offset >= 0 && offset + BOOLEAN_SIZE <= 400) {
         contents.position(offset);
         // just one byte
         byte byteval = (byte) (val ? 1 : 0);
         contents.put(byteval);
      }
      else
         throw new IllegalArgumentException("Given boolean does not fit to page. Please check it again. (Limit: " + BLOCK_SIZE + ")");
   }

   /**
    * Return the float value at the specified offset value of the page.
    * If a float does not stored at that location,
    * the behavior of method is unpredictable.
    * @param offset the byte offset within the page
    * @return the float value at that offset.
    */
   public synchronized float getFloat(int offset) {
      contents.position(offset);
      return contents.getFloat();
   }


   /**
    * Writes a float to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the boolean to be written to the page
    */
   public synchronized void setFloat(int offset, float val) {
      if (offset >= 0 && offset + FLOAT_SIZE <= 400 ) {
         contents.position(offset);
         contents.putFloat(val);
      }
      else
         throw new IllegalArgumentException("Given float does not fit to page. Please check it again. (Limit: " + BLOCK_SIZE + ")");
   }

   /**
    * Return the double value at the specified offset value of the page.
    * If a double does not stored at that location,
    * the behavior of method is unpredictable.
    * @param offset the byte offset within the page
    * @return the double value at that offset.
    */
   public synchronized double getDouble(int offset) {
      contents.position(offset);
      return contents.getDouble();
   }

   /**
    * Writes a double to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the double to be written to the page.
    */
   public synchronized void setDouble(int offset, double val) {
      if (offset >= 0 && offset + DOUBLE_SIZE <= 400) {
         contents.position(offset);
         contents.putDouble(val);
      }
      else
         throw new IllegalArgumentException("Given double does not fit to page. Please check it again. (Limit: " + BLOCK_SIZE + ")");
   }

   /**
    * Return the date object at the specified offset value of the page.
    * If a date does not stored at that location,
    * the behavior of method is unpredictable.
    * @param offset the byte offset within the page
    * @return the date object at that offset.
    */
   public synchronized Date getDate(int offset) {
      contents.position(offset);
      return new Date(contents.getLong());
   }

   /**
    * Writes a date to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the date object to be written to the page.
    */
   public synchronized void setDate(int offset, Date val) {
      if (offset >= 0 && offset + DATE_SIZE <= 400) {
         contents.position(offset);
         contents.putLong(val.getTime());
      }
      else
         throw new IllegalArgumentException("Given date object does not fit to page. Please check it again. (Limit: " + BLOCK_SIZE + ")");
   }

   /**
    * Return the long value at the specified offset value of the page.
    * If a long does not stored at that location,
    * the behavior of method is unpredictable.
    * @param offset the byte offset within the page
    * @return the long value at that offset.
    */
   public synchronized long getLong(int offset) {
      contents.position(offset);
      return contents.getLong();
   }

   /**
    * Writes a long to the specified offset on the page.
    * @param offset the byte offset within the page
    * @param val the long value to be written to the page.
    */
   public synchronized void setLong(int offset, long val) {
      if (offset >= 0 && offset + LONG_SIZE <= 400) {
         contents.position(offset);
         contents.putLong(val);
      }
      else
         throw new IllegalArgumentException("Given long value does not fit to page. Please check it again. (Limit: " + BLOCK_SIZE + ")");
   }
}
