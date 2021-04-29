package com.lisz.hadoop.mapreduce.wc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
	// 这里这个result也是将会在context.write中被序列化，所以可以写到外面
	private IntWritable result = new IntWritable();
	// Reduce中相同的key为一组，这一组数据调用一次reduce方法
	/*
	hello 1
	hello 1
	hello 1
	...
	 */
	// 不精确的理解方法就是key是这个hello，而values就是这一堆1
	public void reduce(Text key, Iterable<IntWritable> values,
                       Context context) throws IOException, InterruptedException {
		int sum = 0;
		for (IntWritable val : values) {
            sum += val.get();
        }
        result.set(sum);
        context.write(key, result);
    }

}
