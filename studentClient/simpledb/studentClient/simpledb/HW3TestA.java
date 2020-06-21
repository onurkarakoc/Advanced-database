package studentClient.simpledb;
import simpledb.file.*;
import simpledb.server.SimpleDB;
import simpledb.file.*;
import simpledb.log.LogMgr;
import simpledb.buffer.BufferMgr;
import simpledb.buffer.PageFormatter;
import simpledb.tx.Transaction;
import java.util.*;

public class HW3TestA {
	private static Collection<Transaction> uncommittedTxs = new ArrayList<>();
	
	public static void main(String[] args) {
		SimpleDB.init("studentdb");
		PageFormatter pf = new IntFormatter();
		Transaction[]  txs = new Transaction[18];
		Transaction master = new Transaction();
	
		for (int i=2; i<18; i++) {
			Block blk= master.append("junk", pf);
			txs[i] = new Transaction();
			uncommittedTxs.add(txs[i]);
			txs[i].pin(blk);
			int x = txs[i].getInt(blk, 0);
			txs[i].setInt(blk, 0, 1000+i);
			System.out.println("transaction " + i + " setint old=" + x + " new=" + 1000+i);
			txs[i].unpin(blk);
			if (i%3==2)
				pareDown();
		}	
		master.commit();
	}

	// commit half of the uncommitted txs
	private static void pareDown() {
		Iterator<Transaction> iter = uncommittedTxs.iterator();
		int count = 0;
		while (iter.hasNext()) { // loop back to the beginning
			Transaction tx = iter.next();
			if (count % 2 == 0) {
				tx.commit();
				iter.remove();
			}				
			count++;
		}
	}
	
}
