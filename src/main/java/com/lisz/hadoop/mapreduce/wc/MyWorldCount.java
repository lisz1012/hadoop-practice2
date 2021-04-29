package com.lisz.hadoop.mapreduce.wc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
// hadoop jar hadoop-mapreduce-examples-2.10.0.jar wordcount /data/wc/input /data/wc/output
public class MyWorldCount {
	public static void main(String[] args) throws Exception{
		// load the xml files in resources
		Configuration conf = new Configuration(true);

		GenericOptionsParser parser = new GenericOptionsParser(conf, args); // accept the cmd params
		String otherArgs[] = parser.getRemainingArgs(); // parse the params

		// To run the progrma on Mac or Windows as well
		conf.set("mapreduce.app-submission.cross-platform", "true");
		conf.set("mapreduce.framework.name", "yarn");
		System.out.println(conf.get("mapreduce.framework.name"));
		Job job = Job.getInstance(conf);
		// Upload this jar file
		//job.setJar("/Users/shuzheng/IdeaProjects/hadoop-hdfs/target/hadoop-hdfs-1.0-SNAPSHOT.jar");
		// Main class
		job.setJarByClass(MyWorldCount.class);
		job.setJobName("myJob");
		Path infile = new Path(otherArgs[0]);
		TextInputFormat.addInputPath(job, infile);
		Path outfile = new Path(otherArgs[1]);
		if (outfile.getFileSystem(conf).exists(outfile)) {
			outfile.getFileSystem(conf).delete(outfile, true);
		}
		TextOutputFormat.setOutputPath(job, outfile);
		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(Text.class);  // Reflect
		job.setMapOutputValueClass(IntWritable.class);
		job.setReducerClass(MyReducer.class);
		//job.setNumReduceTasks(5);  // Set number of reduce tasks
		job.waitForCompletion(true);
	}
}
