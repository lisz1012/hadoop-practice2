package com.lisz.hadoop.mapreduce.wc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class MyMapper extends Mapper<Object, Text, Text, IntWritable> {
	// 在hadoop框架中，它是个分布式的，数据最终是要做序列化和反序列化。而java里面的基本类型是不做序列化和反序列化的，hadoop就帮着对基本
	// 类型做了封装： String -> Text, int -> IntWritable, null -> NullWritable ...
	// 或者自己开发类型，开发的类型必须实现他的序列化反序列化接口，同时实现比较器接口，开发人员指定比较规则
	// 排序 -> 比较。世界上有两种顺序：字典顺序、数值序
	// 大部分时间重写map方法，偶尔重写setup和cleanup
	// 可以写成成员变量，这是因为：context.write(word, one);这里每次执行都是拿着word和one为模版做序列化
	// 每次序列化出来的字节数组都是新的，互不影响，map后面有一个基于内存的buffer，是一个字节数组
	// 写在这里不用每次都new，这时候性能就节省下来了，不会对GC造成压力
	// 见MapTask的1156行：keySerializer.serialize(key); 然后会将序列化之后的结果放入buffer

	// MapTask 794行这两个缓冲区的设置将来可以调整
	// final float spillper =
	//        job.getFloat(JobContext.MAP_SORT_SPILL_PERCENT, (float)0.8);
	//      final int sortmb = job.getInt(MRJobConfig.IO_SORT_MB,
	//          MRJobConfig.DEFAULT_IO_SORT_MB);

	private final static IntWritable ONE = new IntWritable(1);
    private Text word = new Text();

    // hello hadoop 1
    // hello hadoop 2
	// TextInputFormat
	// key是每一行字符串自己的第一个字节面向源文件的偏移量，value才是这个字符串，所以关心的不是key，而是这个value，而且value要做切分
	// 父类Mapper中的run方法会调用到map方法
	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		// StringTokenizer会把各种空白字符，如 " \t\n\r\f" 作为切割的分隔符
		StringTokenizer itr = new StringTokenizer(value.toString());
        while (itr.hasMoreTokens()) {
            word.set(itr.nextToken());
            context.write(word, ONE);
        }
    }
}
