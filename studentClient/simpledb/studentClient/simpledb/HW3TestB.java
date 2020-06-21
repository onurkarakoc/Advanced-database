package studentClient.simpledb;

import simpledb.buffer.BufferMgr;
import simpledb.file.FileMgr;
import simpledb.log.LogMgr;
import simpledb.server.SimpleDB;
import simpledb.tx.Transaction;

public class HW3TestB {
   public static void main(String[] args) {
	  System.out.println("Initiating Recovery");
	  System.out.println("Here are the visited log records");
	  SimpleDB.init("studentdb"); 
   }
}
