# Yarn

## Introduction
In hadoop 1.x, the mapreduce component starts a job tracker and some task trackers to wait for the coming computation jobs.
This architecture has 3 pain points: 1. Single point failure for job tracker. 2. Job tracker could be over loaded when there
a number of jobs submitted. 3. Resource management is coupled with task management, different computation frameworks, such
as MR and Spark etc, can't reuse the resource management, and they don't know the amount of resources consumed by other 
computation frameworks running at the same time. So Yarn was designed to accommodate all computation frameworks
and resolve the above issues.   

Mapreduce and Spark are both batch computation frameworks running on a huge amount of data. So we should move the computation to data.
HDFS can expose the location of the data and other roles should manage the resource and tasks. Before hadoop 2.x, there
are job tracker and task trackers to do such managements, but starting from hadoop 2.x, yarn was introduced to manage the
resource and it will assign a task to an Application Master, which will manage the process of the task. As a result, the 
managements of resource and task are decoupled from each other.

## Roles

![Yarn-Images/yarn_architecture.gif](Yarn-Images/yarn_architecture.gif)

### Client
The client is process started in the "main" method of the jar, which also contains the mapper and reducer defined by the
user. Client asks the name node to get the number of blocks with their offsets & locations, and calculate the metadata of
splits. A split will be processed by a map task (1:1), so Yarn will know which mappers will take which splits. The
metadata of a split will be like:
```
split01  File_A   0      500    node1, node3, node5
split02  File_A   500    1000   node2, node4, node5
...
split08  File_B   0      500    node6, node7, node9
...
``` 
The client will submit the metadata of the splits to Yarn, but it does not know the location where the computation will
occur, since client it provides a list of the locations of the splits. Client does not receive any heartbeat 
from each node in the hadoop cluster, so it does not know the availability of each of them, and Yarn will assign the
resource of the computation later.  

The client will also create the configuration files of the computation and upload them together together with jar and
split metadata to HDFS (moving computation to data). The client tells the Resource Manager about the location of such 
files when it submits the job.  

### Resource and Task Management
Resource Manager and Node Managers, Master - Slave model. With the Resource Manager, computation frameworks will have the same view about the resource 
on all the nodes. Client connects to the resource manager, and resource manager will spawn an Application Master (A like s Job Tracker
without resource management, and is started on demand) on a node which is not busy. Application Master downloads the split 
metadata from HDFS, and apply resources from the Resource Manager, which has all details about the 
current resource usage of the cluster. Resource Manager will send requests to some selected Node Managers to start a Container on each 
of them. A Container constrains the amount of resource could be used by a job on the current node, it will register itself
to the Application Master (which is also a Container). Application Master then sends a message to let the Containers fetch
the jar files and reflect the Mapper/Reducer classes to objects to run the job.  

Application Masters of different jobs could be located on different nodes, so if one node of an Application Master goes down, 
other Application Masters and their jobs are not affected. With multiple Application Masters, the load of the task 
management is also balanced.  

The Containers (including the Application Master) could fail during the computation, and Application Master wil not receive
the heartbeat from these containers any more, so it will reapply resources from the Resource Manager and new Containers 
will be started and they will register themselves to this Application Master.   

Node Manager has a thread which monitors the resource usage of the containers, if a container exceeds the limit, it 
will be killed. In this case, the client should assign more resources in the config and restart the job. (kernel can also
constrain the usage of the resources on the node with cgroup, so the resource management can integrate with Docker, which
relies on the cgroup of the kernel)

Although Application Masters of different jobs are located on different nodes, Resource Manager is still a single point. 
So zookeeper is usually used to implement the HA of the Resource Manager (like the HA for the name nodes).   

Unlike the Resource Manager and Node Managers, Application Master and Containers will be destroyed after the job completion.
The uploaded jar files and metadata of the splits are also temp files, and will be removed.

## References:
https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html  
https://hadoop.apache.org/docs/r2.10.1/hadoop-yarn/hadoop-yarn-site/NodeManager.html  
https://hadoop.apache.org/docs/r2.10.1/hadoop-yarn/hadoop-yarn-site/ResourceManagerHA.html  
https://hadoop.apache.org/docs/r2.10.1/hadoop-yarn/hadoop-yarn-site/ResourceManagerRestart.html  
https://hadoop.apache.org/docs/r2.10.1/hadoop-yarn/hadoop-yarn-site/SecureContainer.html  
https://hadoop.apache.org/docs/r2.10.1/hadoop-yarn/hadoop-yarn-site/OpportunisticContainers.html  
https://hadoop.apache.org/docs/r2.10.1/hadoop-yarn/hadoop-yarn-site/DockerContainers.html  
https://hadoop.apache.org/docs/r2.10.1/hadoop-yarn/hadoop-yarn-site/NodeManagerCgroups.html  