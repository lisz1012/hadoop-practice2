package com.lisz.hadoop.mapreduce.topn;

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class TopNSortingComparator extends WritableComparator {
	public TopNSortingComparator() {
		super(TopNKey.class, true);
	}

	@Override
	public int compare(WritableComparable a, WritableComparable b) {
		TopNKey t1 = (TopNKey)a;
		TopNKey t2 = (TopNKey)b;
		int c1 = Integer.compare(t1.getYear(), t2.getYear());
		if (c1 == 0) {
			int c2 = Integer.compare(t1.getMonth(), t2.getMonth());
			if (c2 == 0) {
				return Integer.compare(t2.getTemperature(), t1.getTemperature());
			}
			return c2;
		}
		return c1;
	}
}
