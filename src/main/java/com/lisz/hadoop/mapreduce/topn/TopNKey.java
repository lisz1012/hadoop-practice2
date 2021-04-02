package com.lisz.hadoop.mapreduce.topn;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TopNKey implements WritableComparable<TopNKey> {
	private int year;
	private int month;
	private int day;
	private int temperature;

	public TopNKey() {
	}

	public TopNKey(int year, int month, int day, int temperature) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.temperature = temperature;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	@Override
	public int compareTo(TopNKey o) {
		int c1 = Integer.compare(year, o.year);
		if (c1 == 0) {
			int c2 = Integer.compare(month, o.month);
			if (c2 == 0) {
				return Integer.compare(day, o.day);
			}
			return c2;
		}
		return c1;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(year);
		out.writeInt(month);
		out.writeInt(day);
		out.writeInt(temperature);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		year = in.readInt();
		month = in.readInt();
		day = in.readInt();
		temperature = in.readInt();

	}
}
