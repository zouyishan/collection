# NameServer架构设计

消息中间件的设计思路一般基于主题的订阅发布机制，消息生产者(Producer)发送某一主题的消息到消息服务器，消息服务器复杂该消息的持久化存储，消息消费者(Consumer)订阅感兴趣的主题，**消息服务器根据订阅信息和路由信息将消息推送(PUSH)到消费者或者消息消费者主动向服务器拉取消息(PULL模式)，从而实现生产者与消费者之间的解耦**。为了避免消息服务器的单点故障导致的整个系统瘫痪，通常会部署多台消息服务器共同承担消息的存储。**那消息生产者如何知道消息要发往哪台消息服务器呢？如果某一台消息服务器宕机了，那么生产者如何在不重启服务的情况下感知呢**？

Nameserver就是为了解决这些问题：

![image-20210522190517138](/Users/bytedance/Library/Application Support/typora-user-images/image-20210522190517138.png)

Broker消息服务器在启动时向所有NameServer注册，消息生产者在发送消息之前先从NameServer获取Broker服务器地址列表，然后根据负载算法从列表中选择一台消息服务器进行消息发送。**NameServer与每台Broker服务器保持长连接，并间隔30s检测Broker是否存活，如果检测到宕机就将其移除**。

**NameServer本身的高可用可通过部署多台NameServer服务器来实现，但彼此之间互不通信，RocketMQ NameServer设计追求简单高效**。



# NameServer启动流程

先从启动类开始，`org.apache.rocketmq.namesrv.NamesrvStartup`。

```java
public class NamesrvStartup {    
		public static void main(String[] args) {
        main0(args);
    }

  
    public static NamesrvController main0(String[] args) {
        try {
            NamesrvController controller = createNamesrvController(args);
            start(controller);
            String tip = "The Name Server boot success. serializeType=" + RemotingCommand.getSerializeTypeConfigInThisServer();
            log.info(tip);
            System.out.printf("%s%n", tip);
            return controller;
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }
}
```

我们一步一步来，main函数主要有两个功能



**一个是初始化先对命令行参数进行解析**

看看`createNamesrvController`方法。这是个长代码，主要是对参数`-c`和`-p`的解析。我们先挑重要的来说

```java
public static NamesrvController createNamesrvController(String[] args) throws IOException, JoranException {
  System.setProperty(RemotingCommand.REMOTING_VERSION_KEY, Integer.toString(MQVersion.CURRENT_VERSION));
  //PackageConflictDetect.detectFastjson();

  Options options = ServerUtil.buildCommandlineOptions(new Options());
  commandLine = ServerUtil.parseCmdLine("mqnamesrv", args, buildCommandlineOptions(options), new PosixParser());
  if (null == commandLine) {
    System.exit(-1);
    return null;
  }

  final NamesrvConfig namesrvConfig = new NamesrvConfig();
  final NettyServerConfig nettyServerConfig = new NettyServerConfig();
  nettyServerConfig.setListenPort(9876);
  if (commandLine.hasOption('c')) {
    String file = commandLine.getOptionValue('c');
    if (file != null) {
      InputStream in = new BufferedInputStream(new FileInputStream(file));
      properties = new Properties();
      properties.load(in);
      MixAll.properties2Object(properties, namesrvConfig);
      MixAll.properties2Object(properties, nettyServerConfig);

      namesrvConfig.setConfigStorePath(file);

      System.out.printf("load config properties file OK, %s%n", file);
      in.close();
    }
  }

  // p参数用于打印对象的值，多用于debug，debug完就推出了。
  if (commandLine.hasOption('p')) {
    InternalLogger console = InternalLoggerFactory.getLogger(LoggerName.NAMESRV_CONSOLE_NAME);
    MixAll.printObjectProperties(console, namesrvConfig);
    MixAll.printObjectProperties(console, nettyServerConfig);
    System.exit(0);
  }

  MixAll.properties2Object(ServerUtil.commandLine2Properties(commandLine), namesrvConfig);

  if (null == namesrvConfig.getRocketmqHome()) {
    System.out.printf("Please set the %s variable in your environment to match the location of the RocketMQ installation%n", MixAll.ROCKETMQ_HOME_ENV);
    System.exit(-2);
  }

  LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
  JoranConfigurator configurator = new JoranConfigurator();
  configurator.setContext(lc);
  lc.reset();
  configurator.doConfigure(namesrvConfig.getRocketmqHome() + "/conf/logback_namesrv.xml");

  log = InternalLoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);

  MixAll.printObjectProperties(log, namesrvConfig);
  MixAll.printObjectProperties(log, nettyServerConfig);

  final NamesrvController controller = new NamesrvController(namesrvConfig, nettyServerConfig);

  // remember all configs to prevent discard
  controller.getConfiguration().registerConfig(properties);

  return controller;
}
```

开始我们可以看到是先读取了命令行参数，如果是空的话就直接退出。

**"-c"**:  的逻辑从代码我们可以知道先创建`NameServerConfig`(NameServer业务参数)，`NettyServerConfig`(NameServer参数)，然后在解析启动时把指定的配置文件或启动命令中得到选项值，填充到`nameServerConfig`，`nettyServerConfig`对象。参数来源有两种方式：

* -c configFile指定配置文件的路径
* 使用"--属性名 属性值"，例如：--listenPort 9876

**-p**: 的逻辑就是打印查看配置的参数，多用于debug使用。



**一个是对Controller的初始化**

```java
start(controller); 
public static NamesrvController start(final NamesrvController controller) throws Exception {

  if (null == controller) {
    throw new IllegalArgumentException("NamesrvController is null");
  }

  boolean initResult = controller.initialize();
  if (!initResult) {
    controller.shutdown();
    System.exit(-3);
  }

  Runtime.getRuntime().addShutdownHook(new ShutdownHookThread(log, new Callable<Void>() {
    @Override
    public Void call() throws Exception {
      controller.shutdown();
      return null;
    }
  }));

  controller.start();

  return controller;
}
```

根据解析出的配置参数，调用`controller.initialize()`来初始化，然后调用`controller.start()`让`NameServer`开始服务。

还有一个逻辑就是注册`ShutdownHookThread`，当程序退出的时候会调用`controller.shutdown`来做退出前的清理工作。



# NameServer的总控制逻辑

NameServer的总控制逻辑在NamesrvController.java代码中。NameServer是集群的协调者，**它只是简单地接收其他角色报上来的状态，然后根据请求返回相应的状态**。

首先，NameserverController把线程池初始化好：

```java
this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
  @Override
  public void run() {
    NamesrvController.this.routeInfoManager.scanNotActiveBroker();
  }
}, 5, 10, TimeUnit.SECONDS);

this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
  @Override
  public void run() {
    NamesrvController.this.kvConfigManager.printAllPeriodically();
  }
}, 1, 10, TimeUnit.MINUTES);
```

启动一个默认是8个线程的线程池(private int serverWorkerThreads = 8)，还有两个定时执行的线程，一个用来扫描失效的Broker(scanNotActiveBroker),另一个用来打印配置信息(printAllPeriodically)。



然后启动负责通信的服务remotingServer，remotingServer监听一些端口，收到Broker，Client等发过来的请求后，根据请求的命令，调用不同的Processor来处理。这些不同的处理逻辑被放到上面初始化线程池中执行：

```java
this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.brokerHousekeepingService);

this.remotingExecutor =
  Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new ThreadFactoryImpl("RemotingExecutorThread_"));

this.registerProcessor();

private void registerProcessor() {
  if (namesrvConfig.isClusterTest()) {
    this.remotingServer.registerDefaultProcessor(new ClusterTestRequestProcessor(this, namesrvConfig.getProductEnvName()), this.remotingExecutor);
  } else {
    this.remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(this), this.remotingExecutor);
  }
}
```

remotingServer是基于Netty封装的一个网络通信服务，要了解remotingServer需要对Netty有一定的认知。



# 核心业务逻辑处理

**NameServer的核心业务逻辑，在`DefaultRequestProcessor.java`中可以一目了然的看出**。网络通信服务模块收到请求后，就调用Processor来处理：

```java
switch (request.getCode()) {
  case RequestCode.PUT_KV_CONFIG:
    return this.putKVConfig(ctx, request);
  case RequestCode.GET_KV_CONFIG:
    return this.getKVConfig(ctx, request);
  case RequestCode.DELETE_KV_CONFIG:
    return this.deleteKVConfig(ctx, request);
  case RequestCode.QUERY_DATA_VERSION:
    return queryBrokerTopicConfig(ctx, request);
  case RequestCode.REGISTER_BROKER:
    Version brokerVersion = MQVersion.value2Version(request.getVersion());
    if (brokerVersion.ordinal() >= MQVersion.Version.V3_0_11.ordinal()) {
      return this.registerBrokerWithFilterServer(ctx, request);
    } else {
      return this.registerBroker(ctx, request);
    }
  case RequestCode.UNREGISTER_BROKER:
    return this.unregisterBroker(ctx, request);
  case RequestCode.GET_ROUTEINFO_BY_TOPIC:
    return this.getRouteInfoByTopic(ctx, request);
  case RequestCode.GET_BROKER_CLUSTER_INFO:
    return this.getBrokerClusterInfo(ctx, request);
  case RequestCode.WIPE_WRITE_PERM_OF_BROKER:
    return this.wipeWritePermOfBroker(ctx, request);
  case RequestCode.GET_ALL_TOPIC_LIST_FROM_NAMESERVER:
    return getAllTopicListFromNameserver(ctx, request);
  case RequestCode.DELETE_TOPIC_IN_NAMESRV:
    return deleteTopicInNamesrv(ctx, request);
  case RequestCode.GET_KVLIST_BY_NAMESPACE:
    return this.getKVListByNamespace(ctx, request);
  case RequestCode.GET_TOPICS_BY_CLUSTER:
    return this.getTopicsByCluster(ctx, request);
  case RequestCode.GET_SYSTEM_TOPIC_LIST_FROM_NS:
    return this.getSystemTopicListFromNs(ctx, request);
  case RequestCode.GET_UNIT_TOPIC_LIST:
    return this.getUnitTopicList(ctx, request);
  case RequestCode.GET_HAS_UNIT_SUB_TOPIC_LIST:
    return this.getHasUnitSubTopicList(ctx, request);
  case RequestCode.GET_HAS_UNIT_SUB_UNUNIT_TOPIC_LIST:
    return this.getHasUnitSubUnUnitTopicList(ctx, request);
  case RequestCode.UPDATE_NAMESRV_CONFIG:
    return this.updateConfig(ctx, request);
  case RequestCode.GET_NAMESRV_CONFIG:
    return this.getConfig(ctx, request);
  default:
    break;
}
```

逻辑主体是个switch语句，根据RequestCode调用不同的函数来处理，从RequestCode可以了解到NameServer的主要功能，比如：

* REGISTER_BROKER是在集群中新加入一个Borker
* GET_ROUTEINTO_BY_TOPIC是请求获取一个Topic的路由信息
* WIPE_WRITE_PERM_OF_BROKER是删除一个Broker的写权限



# 集群状态存储

NameServer作为集群的协调者，需要保护和维护集群的各种元数据，这是通过RouteInfoManager类来实现：

```java
private final ReadWriteLock lock = new ReentrantReadWriteLock();
private final HashMap<String/* topic */, List<QueueData>> topicQueueTable;
private final HashMap<String/* brokerName */, BrokerData> brokerAddrTable;
private final HashMap<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;
private final HashMap<String/* brokerAddr */, BrokerLiveInfo> brokerLiveTable;
private final HashMap<String/* brokerAddr */, List<String>/* Filter Server */> filterServerTable;

public RouteInfoManager() {
  this.topicQueueTable = new HashMap<String, List<QueueData>>(1024);
  this.brokerAddrTable = new HashMap<String, BrokerData>(128);
  this.clusterAddrTable = new HashMap<String, Set<String>>(32);
  this.brokerLiveTable = new HashMap<String, BrokerLiveInfo>(256);
  this.filterServerTable = new HashMap<String, List<String>>(256);
}
```

每个结构存储着一类集群信息。下面重点看一下控制访问这些结构的锁机制。

锁分为互斥锁，读写锁；也可分为可重入锁，不可重入锁。**在NameServer的场景中，读操作很多，更改操作少，所以选择读写锁能大大提高效率**。RouteInfoManager中使用的是可重入的写锁(`private final ReadWriteLocak lock = new ReentrantReadWriteLock()`),我们以deleteTopic函数为例，看一下锁的使用方式：

```java
public void deleteTopic(final String topic) {
  try {
    try {
      this.lock.writeLock().lockInterruptibly();
      this.topicQueueTable.remove(topic);
    } finally {
      this.lock.writeLock().unlock();
    }
  } catch (Exception e) {
    log.error("deleteTopic Exception", e);
  }
}
```

首先锁的获取和执行逻辑要放到一个try{}里，然后在finally{}中释放。
