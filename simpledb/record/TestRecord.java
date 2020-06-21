package simpledb.record;

import simpledb.metadata.MetadataMgr;
import simpledb.server.SimpleDB;
import simpledb.tx.Transaction;

import java.util.*;

public class TestRecord {
    public static void main(String[] args) {
        // Initiate the database
        SimpleDB.init("studentdb");
        MetadataMgr metadataMgr = SimpleDB.mdMgr();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a table name: ");
        // Takes table name from the user.
        String tableName = scanner.nextLine();
        Transaction tx = new Transaction();
        TableInfo ti = SimpleDB.mdMgr().getTableInfo(tableName, tx);
        RecordFile recordFile = new RecordFile(ti, tx);
        // Positions after last record.
        recordFile.afterLast();
        // If it is possible search record page backwards.
        while(recordFile.previous()) {
           for(String field: ti.schema().fields()) {
               System.out.println(field + ": " + recordFile.getInt(field));

           }
       }
    }
}
