package com.lisz.hadoop.mapreduce.wc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class MyMapper extends Mapper<Object, Text, Text, IntWritable> {
	// MapTask line 1156：keySerializer.serialize(key);

	private final static IntWritable ONE = new IntWritable(1);
    private final static Text WORD = new Text();

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		// StringTokenizer will split the string on " \t\n\r\f"
		StringTokenizer itr = new StringTokenizer(value.toString());
        while (itr.hasMoreTokens()) {
	        WORD.set(itr.nextToken());
            context.write(WORD, ONE);
        }
    }
}
