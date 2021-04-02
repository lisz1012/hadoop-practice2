package com.lisz.hadoop.mapreduce.topn;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class TopNPartitioner extends Partitioner<TopNKey, IntWritable> {

	@Override
	public int getPartition(TopNKey topNKey, IntWritable intWritable, int numPartitions) {
		return (topNKey.getYear() + topNKey.getMonth()) % numPartitions;
	}
}
