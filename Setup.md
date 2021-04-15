# Hadoop Setup：

## Operating System and Software

1. centos 7 (https://www.centos.org/download/)
2. jdk 1.8 (https://www.oracle.com/java/technologies/javase-downloads.html)  
3. hadoop 2.6.5 (https://hadoop.apache.org/releases.html)  

## Prerequisite：
	
#### LAN IP
		
`vi /etc/sysconfig/network-scripts/ifcfg-ens-33`  

```
   TYPE="Ethernet"
   BOOTPROTO="static"
   DEFROUTE="yes"
   PEERDNS="yes"
   PEERROUTES="yes"
   IPV4_FAILURE_FATAL="no"
   IPV6INIT="yes"
   IPV6_AUTOCONF="yes"
   IPV6_DEFROUTE="yes"
   IPV6_PEERDNS="yes"
   IPV6_PEERROUTES="yes"
   IPV6_FAILURE_FATAL="no"
   IPV6_ADDR_GEN_MODE="stable-privacy"
   NAME="ens33"
   UUID="10307a66-248d-4480-add7-f4f53669c7af"
   DEVICE="ens33"
   ONBOOT="yes"
   IPADDR="192.168.1.6"
   GATEWAY="192.168.1.1"
   NETMASK="255.255.255.0"
   DNS1=192.168.1.1 
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
    192.168.1.6     hadoop-01
    192.168.1.7     hadoop-02
    192.168.1.8     hadoop-03
    192.168.1.9     hadoop-04
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

#### Setup passphraseless ssh： 

`ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa`  
  
`cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys`  

If host A would like to ssh to B without inputing the password, it needs to add its own public key to the authorized_keys file on host B
```
    A：
        ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
        cd ~/.ssh
        scp id_dsa.pub hadoop-02:/root/.ssh
    B：
        cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
```

## Hadoop Setup

### Installation
`mkdir /opt/bigdata`  
`tar xf hadoop-2.6.5.tar.gz`  
`mv hadoop-2.6.5  /opt/bigdata/`  
`pwd`  
    `/opt/bigdata/hadoop-2.6.5`  

`vi /etc/profile`  	
```
    export  JAVA_HOME=/usr/java/default
    export HADOOP_HOME=/opt/bigdata/hadoop-2.6.5
    export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```
`source /etc/profile`
	
### Hadoop Roles：
`cd   $HADOOP_HOME/etc/hadoop`  
`vi hadoop-env.sh`
```    
    export JAVA_HOME=/usr/java/default
```

`vi core-site.xml`  
```
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://hadoop-01:9000</value>
    </property>
```

`vi hdfs-site.xml`  
```
        <property>
            <name>dfs.replication</name>
            <value>2</value>
        </property>
        <property>
            <name>dfs.namenode.name.dir</name>
            <value>/var/bigdata/hadoop/local/dfs/name</value>
        </property>
        <property>
            <name>dfs.datanode.data.dir</name>
            <value>/var/bigdata/hadoop/local/dfs/data</value>
        </property>
```

### IP of Data Nodes
`vi slaves`  
```
  hadoop-02
  hadoop-03
  hadoop-04 
```

### Copy The Settings and Files to Other Nodes
1. OS
2. JDK
3. Hadoop
4. ssh
5. Network
6. Hadoop xml files

### Format
`hdfs namenode -format`  
HDFS will create directories and initialize a fsimage	

## Start HDFS	
`start-dfs.sh`  
Namenode and datanodes will initialize the directories and files
		
View http://hadoop-01:50070 to verify if the HDFS is started successfully.

## Simple Commands
```
	hdfs dfs -mkdir /bigdata
	hdfs dfs -mkdir  -p  /user/root
```
