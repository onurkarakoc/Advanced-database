package simpledb.file;

import simpledb.server.SimpleDB;

import java.util.Date;

public class G06_test {
    public static void main(String[] args) {
        SimpleDB.init("studentdb");
        FileMgr fm = SimpleDB.fileMgr();
        int fileSize = fm.size("junk");
        Block block = new Block("junk", fileSize);

        G06_updatedPage p1 = new G06_updatedPage();
        p1.read(block);
        int n = p1.getInt(50);
        p1.write(block);

        G06_updatedPage p2 = new G06_updatedPage();
        p2.setString(20, "hello");
        p2.setDate(100, new Date());
        block = p2.append("junk");

        G06_updatedPage p3 = new G06_updatedPage();
        p3.read(block);
        String exampleString = p3.getString(20);
        Date exampleDate = p3.getDate(100);
        System.out.println("Block " + block.number() + " contains " + exampleString );
        System.out.println("Block " + block.number() + " contains " + exampleDate );

        // Test functions in Page Class

        G06_updatedPage newPage = new G06_updatedPage();
        newPage.setBoolean(5, true);
        newPage.setDate(25, new Date());
        newPage.setDouble(50, 5.3);
        System.out.println("Page offset: 5, Boolean value: " + newPage.getBoolean(5));
        // This line prints the current date object in the page.
        System.out.println("Page offset: 25, Date value: " + newPage.getDate(25));
        System.out.println("Page offset: 50, Double value: " + newPage.getDouble(50));

        // Boundary test
        G06_updatedPage boundaryTest = new G06_updatedPage();
        //This line throws an illegal argument exception because of overflow. (Computation: 4 + 396 + size of string)
        boundaryTest.setString(396, "test");
    }
}
