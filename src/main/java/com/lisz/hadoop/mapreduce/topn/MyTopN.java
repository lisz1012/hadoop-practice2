package com.lisz.hadoop.mapreduce.topn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MyTopN {
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration(true);
		Job job = Job.getInstance(conf);
		String[] other = new GenericOptionsParser(conf, args).getRemainingArgs();
		job.setJarByClass(MyTopN.class);
		job.setJobName("My top N");

		TextInputFormat.addInputPath(job, new Path(other[0]));
		Path outfile = new Path(other[1]);
		if (outfile.getFileSystem(conf).exists(outfile)) outfile.getFileSystem(conf).delete(outfile, true);
		TextOutputFormat.setOutputPath(job, outfile);

		job.setMapperClass(TopNMapper.class);
		job.setMapOutputKeyClass(TopNKey.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setPartitionerClass(TopNPartitioner.class);
		job.setSortComparatorClass(TopNSortingComparator.class);

		job.setGroupingComparatorClass(TopNGroupingComparator.class);
		job.setReducerClass(TopNReducer.class);

		job.waitForCompletion(true);
	}
}
/*
1970-8-8	32
1970-8-23	23
2018-3-11	18
2018-4-23	22
2019-5-21	33
2019-6-1	39
2019-6-2	31
 */
