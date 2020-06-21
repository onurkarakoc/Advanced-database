package simpledb.buffer;

import simpledb.file.Block;
import simpledb.server.SimpleDB;

import java.util.HashMap;
import java.util.Map;

public class BufferTest {
    private static Map<Block,Buffer> buffs = new HashMap<>();
    private static BufferMgr bm;

    public static void main(String args[]) throws Exception {
        SimpleDB.init("studentdb");
        bm = SimpleDB.bufferMgr();
        pinBuffer(0); pinBuffer(1); pinBuffer(2); pinBuffer(3);
        pinBuffer(4); pinBuffer(5); pinBuffer(6); pinBuffer(7);
        bm.printStatus();
        unpinBuffer(2); unpinBuffer(0); unpinBuffer(5); unpinBuffer(4);
        bm.printStatus();
        pinBuffer(8); pinBuffer(5); pinBuffer(7);
        bm.printStatus();
    }

    private static void pinBuffer(int i) {
        Block blk = new Block("junk", i);
        Buffer buff = bm.pin(blk);
        buffs.put(blk, buff);
        System.out.println("Pin block " + i);
    }

    private static void unpinBuffer(int i) {
        Block blk = new Block("junk", i);
        Buffer buff = buffs.remove(blk);
        bm.unpin(buff);
        System.out.println("Unpin block " + i);
    }
}


