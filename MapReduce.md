# MapReduce

HDFS is designed for MapReduce, in other words, it's not necessary to design and invite HDFS without MapReduce. HDFS provides the data layer for the MapReduce jobs which is at the computation layer. HDFS supports the random access to the files, as a result, many splits (a split in Map Reduce is a block in HDFS by default) in map reduce can be processed in parallel during the map phase. There are usually 2 phases in MapReduce: Map and Reduce (not necessary). Reduce tasks start after all the map tasks are completed successfully.

## Map

### Split

By default it is a block in HDFS, but the size of a split can also be customized, because there are 2 major types of jobs: `IO intensive` and `CPU intensive` . Large splits are friendly to IO intensive jobs but not for CPU intensive ones, while small splits could give the CPUs a more acceptable load in each job. So in some cases, we customize the size of split to fit our jobs, and a split could be larger or smaller than a block. Split is a logical part while block is a physical part of a file in HDFS, the meta data (including offset, size and location etc) of the blocks can be reused by the splits. The location info of the bocks supports the movement of the map computation to data. 

### Record

The unit to be processed by a `map()` method, depends on the format, the content of a split can be "split" on '\n' (LineRecordReader) or some html tags, like `<p>...</p>`  etc. The `map()` method is invoked for every record writing a k-v pair to a ring buffer as its output when it's invoked each time.

### Group

Defined by the key. After the map method, records with the same key go to the same group and eventually to the same reducer/partition. The `reduce()` method is invoked for each group.

### Map Task

The map task (dash rectangles on the left part to the graph below) is not equivalent to the `map()` method in the mapreduce programs. The number of map tasks (parallelism) depends on the number of splits.

### Map Method

Processes each record and convert it to K-V pairs. After the `map()` method, the partition of the K-V pair will be calculated using its key, so **K-V pairs with the same key will go to the same partition and processed by the same reducer**. The `K-V-P` result (K-V with partition number) will be eventually saved into one file locally rather than to HDFS. Before it is saved as a file, the K-V-Ps will be stored in a buffer in memory (ring buffer, 100M by default) to avoid frequent disk IO.

### Sorting

Once the ring buffer is full, the data will be spilled to disk. During each spill, to save the IO time at the `mapper` side, the data in the memory buffer needs a sorting by the partitions before the flush, otherwise, each reduce tasks will open the file and scan the entire file to collect all the K-V pairs with the target partition number. 

**Benefit**

*Without any sorting, each reduce task will open the result file at mapper side and scan the entire file to collect all the K-V pairs with the target partition number.* *The amount of disk IO = `file size * # of reducers` . With the sorting, K-V pairs with the same partition number will be put next to each other, and each reduce task only needs to pull the part of the file for its own partition number, and the amount of disk IO = `file size * 1` . Although this sorting increases the time complexity, since it happens in memory, and the addressing cost on hard disk (~ms) is over 100k times larger than that in memory (~ns), the sorting can reduce the time cost dramatically*

### Secondary Sorting

To save the IO time at the `reducer` side, it's better to sort the K-V pairs by the keys belonging to each partition before they are spilled to the disk of the mappers.  

**Benefit**

*The number of invocations of the `reduce()` method at a reducer depends on the number of distinct keys for all the K-V pairs in the files sent to it, so reducer has to fully scan these files to collect them if they are not sorted by keys. With the sorting by keys here, K-V pairs with the same key will be put next to each other, making it easier for reducers to group them across all input files for the reduce task, disk IO = `file size * 1`* 

### **Combination (Optional)**:

After the secondary sorting, the records with the same key are next to each other, so they can be easily "reduced" locally in the mapper before spilling to disk, for example, in the word count:

```java
hello 1
hello 1    ---->   hello 2 
```

This can reduce  the file size and the network IO in shuffle.

### Merge on Disk

After the map task complete its last split, there could be multiple files (with sorted keys) created. The files are then merged and saved in the mapper host locally as the result file. With the merge, the resulting file can be read continuously, without the random accesses across many small files. 

Seems it's also ok if we create multiple files and each one is for a partition, the reducers just needs to pull the file(s) belong to it. But if there are too many partitions(reducers), mapper will have to create many smaller files which will cause the random read/write on the disk, and make the efficiency much lower during "shuffle". 

High Level MapReduce：

![MapReduce%20b9b6c62d3ed44e068ef95871c8b7410f/Untitled.png](MapReduce%20b9b6c62d3ed44e068ef95871c8b7410f/Untitled.png)

Number of split : number of map task = 1:1. The number of reducers can be configured. Dashed Rectangles are map/reduce **tasks**, while solid blue ones are map/reduce **methods**. A reduce task is also called a "partition"

```java
    block : split
		 1:1
		 N:1
		 1:N
    split : map
		 1:1
	  map : reduce
		 N:1
		 N:N
		 1:1
		 1:N
```

1 Map and 1 Reduce：

![MapReduce%20b9b6c62d3ed44e068ef95871c8b7410f/Untitled%201.png](MapReduce%20b9b6c62d3ed44e068ef95871c8b7410f/Untitled%201.png)

Note: The output of a map for each record is `K-V-P`

## Reduce

### Input Data

Input data of a reducer includes all the files with the same partition number pulled from all the mappers, they are all sorted by the keys already, so will be easy to merge and keep the order at the same time.

### Reduce Task

A reduce task is located at a partition and can process multiple groups in serial. The default parallelism of the reduce tasks is 1, so developers need to estimate the job and the resource he owns to figure out a proper number of the reduce tasks. Usually the number of reduce tasks is not the same as the number of keys(groups), as there could be 1 billion keys extracted from the data and each key has only 2 records, but we only have 10 hosts as reducers, so in average, each reduce task will process 200M records rather than just 2. K-V pairs with the same key will be in the same group in map phase and go to the same partition and be processed by the same reduce task. One partition can accept K-V pairs with different keys: 

```java
# group(key) : # partition
		  1      :     1
		  N      :     1
		  N      :     M
```

### Reduce Method

In each reduce task, a reduce method can be invoked for multiple times, and each time it processes one group of the k-v pairs which have the same key. Iterator design pattern is applied in the reduce tasks, because the input data of a reduce task could be up to TBs which can't be put into some collections in the memory of any host. The iterator wraps the disk IO of a file, and reads the lines from it. `Iterator Pattern` is a beautiful implementation in batch computing (MR and Spark). 

**Optimization & Tradeoffs**

From graph 2, we can see that to save the time spent on the disk IO, the reducer does not wait for the final result file to be created, and starts the invocation of the `reduce()` method during merging the last 2 files, because like in the merge sort, the merge is easy to give an iterator for a sorted dataset as the output, even if it's not persisted on the disk yet.

In theory, a heap can be applied immediately after the reducer gets all the raw input files pulled from the mappers, and merge K-V pairs across these files and give the iterator of them to the reducer, like the way how HBase merges its StoreFiles. But the number and size of the raw input files are quite unpredictable and the memory in a reducer may not be able to contain the data in its memory (space complexity is O(K), where K is the number of raw input files) . This is not the same case as HBase merges its StoreFiles, since HBase has some settings about the thresholds on the size and amount of its StoreFiles when merging them.

### Example: Word Count

![MapReduce%20b9b6c62d3ed44e068ef95871c8b7410f/Map_Reduce.png](MapReduce%20b9b6c62d3ed44e068ef95871c8b7410f/Map_Reduce.png)