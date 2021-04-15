# Hadoop Setup：

## Operating System and Software

centos 6.5
jdk 1.8 
hadoop 2.6.5 (https://hadoop.apache.org/releases.html)  

## Prerequisite：
	
### Network：

#### IP
		
`vi /etc/sysconfig/network-scripts/ifcfg-ens-33`
    ```
    DEVICE=ens-33
    #HWADDR=00:0C:29:42:15:C2
    TYPE=Ethernet
    ONBOOT=yes
    NM_CONTROLLED=yes
    BOOTPROTO=static
    IPADDR=192.168.1.6
    NETMASK=255.255.255.0
    GATEWAY=192.168.150.2
    DNS1=223.5.5.5
    DNS2=114.114.114.114
    ```
#### Host name:
`vi /etc/sysconfig/network`

```
    NETWORKING=yes
    HOSTNAME=hadoop-01
```
	
#### Host - IP Mappings
`vi /etc/hosts`
```
    192.168.150.11 node01
    192.168.150.12 node02
```
#### Turn off the firewall
execute the command:  
`service iptables stop`  
`chkconfig iptables off`

#### Turn off selinux
`vi /etc/selinux/config`  
    `SELINUX=disabled`
	
#### Sync the time:  
`yum install ntp  -y`

(Optional) `vi /etc/ntp.conf`
    ```
       server ntp1.aliyun.com
    ```	
`service ntpd start`    
`chkconfig ntpd on`  
	
#### Install JDK

`rpm -i   jdk-8u181-linux-x64.rpm`  	
`vi /etc/profile`  
    ```     
        export  JAVA_HOME=/usr/java/default
        export PATH=$PATH:$JAVA_HOME/bin
    ```	
`source /etc/profile`  

	Setup passphraseless ssh： 
		`ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa`  
		`cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys`
		If host A would like to ssh to B without inputing the password, it needs to add its own public key to the authorized_keys file on host B
			A：
				ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
				cd ~/.ssh
				scp id_dsa.pub hadoop-02:/root/.ssh
			B：
				cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
2，Hadoop的配置（应用的搭建过程）
	规划路径：
	mkdir /opt/bigdata
	tar xf hadoop-2.6.5.tar.gz
	mv hadoop-2.6.5  /opt/bigdata/
	pwd
		/opt/bigdata/hadoop-2.6.5
	
	vi /etc/profile	
		export  JAVA_HOME=/usr/java/default
		export HADOOP_HOME=/opt/bigdata/hadoop-2.6.5
		export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
	source /etc/profile
	
	配置hadoop的角色：
	cd   $HADOOP_HOME/etc/hadoop
		必须给hadoop配置javahome要不ssh过去找不到
	vi hadoop-env.sh
		export JAVA_HOME=/usr/java/default
		给出NN角色在哪里启动
	vi core-site.xml
		    <property>
				<name>fs.defaultFS</name>
				<value>hdfs://node01:9000</value>
			</property>
		配置hdfs  副本数为1.。。。
	vi hdfs-site.xml
		    <property>
				<name>dfs.replication</name>
				<value>1</value>
			</property>
			<property>
				<name>dfs.namenode.name.dir</name>
				<value>/var/bigdata/hadoop/local/dfs/name</value>
			</property>
			<property>
				<name>dfs.datanode.data.dir</name>
				<value>/var/bigdata/hadoop/local/dfs/data</value>
			</property>
			<property>
				<name>dfs.namenode.secondary.http-address</name>
				<value>node01:50090</value>
			</property>
			<property>
				<name>dfs.namenode.checkpoint.dir</name>
				<value>/var/bigdata/hadoop/local/dfs/secondary</value>
			</property>


		配置DN这个角色再那里启动
	vi slaves
		node01

3,初始化&启动：
	hdfs namenode -format  
		创建目录
		并初始化一个空的fsimage
		VERSION
			CID
	
	start-dfs.sh
		第一次：datanode和secondary角色会初始化创建自己的数据目录
		
	http://node01:50070
		修改windows： C:\Windows\System32\drivers\etc\hosts
			192.168.150.11 node01
			192.168.150.12 node02
			192.168.150.13 node03
			192.168.150.14 node04

4，简单使用：
	hdfs dfs -mkdir /bigdata
	hdfs dfs -mkdir  -p  /user/root



5,验证知识点：
	cd   /var/bigdata/hadoop/local/dfs/name/current
		观察 editlog的id是不是再fsimage的后边
	cd /var/bigdata/hadoop/local/dfs/secondary/current
		SNN 只需要从NN拷贝最后时点的FSimage和增量的Editlog


	hdfs dfs -put hadoop*.tar.gz  /user/root
	cd  /var/bigdata/hadoop/local/dfs/data/current/BP-281147636-192.168.150.11-1560691854170/current/finalized/subdir0/subdir0
		

	for i in `seq 100000`;do  echo "hello hadoop $i"  >>  data.txt  ;done
	hdfs dfs -D dfs.blocksize=1048576  -put  data.txt 
	cd  /var/bigdata/hadoop/local/dfs/data/current/BP-281147636-192.168.150.11-1560691854170/current/finalized/subdir0/subdir0
	检查data.txt被切割的块，他们数据什么样子
