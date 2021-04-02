package com.lisz.hadoop.mapreduce.topn;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;

public class TopNReducer extends Reducer<TopNKey, IntWritable, Text, IntWritable> {
	private Text text = new Text();
	private IntWritable temperature = new IntWritable();

	@Override
	protected void reduce(TopNKey key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
		Iterator<IntWritable> iterator = values.iterator();
		boolean firstWritten = false;
		int firstDay = key.getDay();
		while (iterator.hasNext()) {
			iterator.next();
			if (!firstWritten) {
				text.set(key.getYear() + "-" + key.getMonth() + "-" + key.getDay());
				temperature.set(key.getTemperature());
				context.write(text, temperature);
				firstWritten = true;
			}
			if (firstDay != key.getDay()) {
				text.set(key.getYear() + "-" + key.getMonth() + "-" + key.getDay());
				temperature.set(key.getTemperature());
				context.write(text, temperature);
				break;
			}
		}
	}
}