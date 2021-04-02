package com.lisz.hadoop.mapreduce.topn;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TopNMapper extends Mapper<LongWritable, Text, TopNKey, IntWritable> {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private Calendar calendar = Calendar.getInstance();
	private TopNKey topNKey = new TopNKey();
	private IntWritable intWritable = new IntWritable();
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		String s = value.toString();
		String strs[] = s.split("\\s+");
		int temperature = Integer.parseInt(strs[3]);
		try {
			Date date = sdf.parse(strs[0]);
			calendar.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		topNKey.setYear(calendar.get(Calendar.YEAR));
		topNKey.setMonth(calendar.get(Calendar.MONTH) + 1);
		topNKey.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		topNKey.setTemperature(temperature);

		intWritable.set(temperature);
		context.write(topNKey, intWritable);
	}
}
