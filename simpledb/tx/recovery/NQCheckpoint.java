package simpledb.tx.recovery;

import simpledb.log.BasicLogRecord;

import java.util.ArrayList;

/**
 * The non-quiescent checkpoint record
 * @author Onur KARAKOC
 */
public class NQCheckpoint implements LogRecord {
    private ArrayList<Integer> txnums;

    /**
     * Creates a non-quiescent checkpoint record for the specified transactions.
     */
    public NQCheckpoint(ArrayList<Integer> txnums) {setTxnums(txnums);}

    public NQCheckpoint(BasicLogRecord rec) {
        txnums = new ArrayList<>();
        int numberOfTransaction = rec.nextInt();
        for (int i = 0; i < numberOfTransaction; i++)
            txnums.add(rec.nextInt());
    }

    /**
     * Write a non-quiescent checkpoint record to the log.
     * This log record contains the NQCKPT operator,
     * followed by the transaction ids.
     * @return the LSN of the last log value.
     */
    public int writeToLog() {
        Object[] rec = new Object[txnums.size() + 2];
        // Just add the sign at the beginning of the non-quiescent checkpoint record.
        rec[0] = NQCKPT;
        // We have two options here to solve determining number of transaction id
        // from the record. One of them is changing the nextInt function in the BasicLogRecord.
        // The other one, also simple, is adding the number of transaction ids to the record.
        rec[1] = txnums.size();
        for (int i = 0; i < txnums.size(); i++)
            rec[i + 2] = txnums.get(i);
        return logMgr.append(rec);
    }

    public int op() {
        return NQCKPT;
    }

    /**
     * Just implemented overridden method
     * with dummy value
     * @return dummy: -1
     * We won't use this function
     */
    public int txNumber() {
        return -1;
    }

    /**
     * We'll use this function for non-quiescent
     * checkpoint record.
     * @return list of transaction ids
     */
    public ArrayList<Integer> txNumbers() {
        return txnums;
    }

    /**
     * Does nothing, because non-quiscent checkpoint
     * record contains no undo information.
     */
    public void undo(int txnum) {

    }

    public String toString() {
        String nqckpt = "<NQCKPT ";
        for (Integer element: txnums) {
            nqckpt += element.toString() + " ";
        }
        nqckpt += ">";
        return nqckpt;
    }

    public void setTxnums(ArrayList<Integer> txnums) {
        this.txnums = txnums;
    }
}
