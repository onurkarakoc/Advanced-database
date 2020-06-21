package studentClient.simpledb;

import simpledb.buffer.PageFormatter;
import simpledb.file.Page;

public class IntFormatter implements PageFormatter {
	 public static final int INT_SIZE = Integer.SIZE / Byte.SIZE;
	@Override
	public void format(Page p) {
		int recsize =  INT_SIZE;
		for (int i=0; i+recsize <= 400; i+=recsize)
			p.setInt(i, i);
		
	}

}
