# 简介

缓存分为本地缓存和远端缓存。常见的远端缓存有Redis，MongoDB；本地缓存一般使用map的方式保存在本地内存中。一般我们在业务中操作缓存，都会操作缓存和数据源两部分。如：put数据时，先插入DB，再删除原来的缓存；ge数据时，先查缓存，命中则返回，没有命中时，需要查询DB，再把查询结果放入缓存中 。如果访问量大，我们还得兼顾本地缓存的线程安全问题。必要的时候也要考虑缓存的回收策略。

今天说的 Guava Cache 是google guava中的一个内存缓存模块，用于将数据缓存到JVM内存中。他很好的解决了上面提到的几个问题：

- 很好的封装了get、put操作，能够集成数据源 ；
- 线程安全的缓存，与ConcurrentMap相似，但前者增加了更多的元素失效策略，后者只能显示的移除元素；
-  Guava Cache提供了三种基本的缓存回收方式：基于容量回收、定时回收和基于引用回收。定时回收有两种：按照写入时间，最早写入的最先回收；按照访问时间，最早访问的最早回收；
- 监控缓存加载/命中情况

Guava Cache的架构设计灵感ConcurrentHashMap，在简单场景中可以通过HashMap实现简单数据缓存，但如果要实现缓存随时间改变、存储的数据空间可控则缓存工具还是很有必要的。Cache存储的是键值对的集合，不同时是还需要处理缓存过期、动态加载等算法逻辑，需要额外信息实现这些操作，对此根据面向对象的思想，还需要做方法与数据的关联性封装，主要实现的缓存功能有：自动将节点加载至缓存结构中，当缓存的数据超过最大值时，使用LRU算法替换；它具备根据节点上一次被访问或写入时间计算缓存过期机制，缓存的key被封装在WeakReference引用中，缓存的value被封装在WeakReference或SoftReference引用中；还可以统计缓存使用过程中的命中率、异常率和命中率等统计数据。


# 先看看实例

```java
package com.zou.cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GuavaCache {

    public static LoadingCache<Integer, Optional<Integer>> setCache() {
        LoadingCache<Integer, Optional<Integer>> cache = CacheBuilder.newBuilder()
                // 设置并发级别为8，并发级别是指可以同时写缓存的线程数
                .concurrencyLevel(8)
                // 设置缓存容器的初始值为10
                .initialCapacity(10)
                // 设置缓存容量的最大值为100
                .maximumSize(100)
                // 设置过期时间
                .expireAfterAccess(5, TimeUnit.SECONDS)
                // 被移除原因
                // 指定CacheLoader，在缓存不存在时同构CacheLoader实现自动加载缓存
                .build(new CacheLoader<Integer, Optional<Integer>>() {
                    @Override
                    public Optional<Integer> load(Integer integer) throws Exception {

                        if (integer < 100) {
                            return Optional.empty();
                        } else {
                            return Optional.of(integer);
                        }
                    }
                });
        return cache;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        LoadingCache<Integer, Optional<Integer>> loadingCache = setCache();
        for (int i = 0; i < 20; i++) {
            System.out.println(loadingCache.getIfPresent(i));
        }
        for (int i = 0; i < 20; i++) {
            loadingCache.put(i, Optional.of(i + 100));
        }

        for (int i = 0; i < 20; i++) {
            System.out.println(loadingCache.get(i));
        }

        TimeUnit.SECONDS.sleep(4);

        for (int i = 0; i < 20; i++) {
            System.out.println(loadingCache.get(i));
        }

        TimeUnit.SECONDS.sleep(2);

        for (int i = 0; i < 20; i++) {
            System.out.println(loadingCache.get(i) + "========");
        }
    }
}
```

输出结果：
```
null
null
null
null
null
null
null
null
null
null
null
null
null
null
null
null
null
null
null
null
Optional[100]
Optional[101]
Optional[102]
Optional[103]
Optional[104]
Optional[105]
Optional[106]
Optional[107]
Optional[108]
Optional[109]
Optional[110]
Optional[111]
Optional[112]
Optional[113]
Optional[114]
Optional[115]
Optional[116]
Optional[117]
Optional[118]
Optional[119]
Optional[100]
Optional[101]
Optional[102]
Optional[103]
Optional[104]
Optional[105]
Optional[106]
Optional[107]
Optional[108]
Optional[109]
Optional[110]
Optional[111]
Optional[112]
Optional[113]
Optional[114]
Optional[115]
Optional[116]
Optional[117]
Optional[118]
Optional[119]
Optional[100]========
Optional[101]========
Optional[102]========
Optional[103]========
Optional[104]========
Optional[105]========
Optional[106]========
Optional[107]========
Optional[108]========
Optional[109]========
Optional[110]========
Optional[111]========
Optional[112]========
Optional[113]========
Optional[114]========
Optional[115]========
Optional[116]========
Optional[117]========
Optional[118]========
Optional[119]========

Process finished with exit code 0
```
这里用的是`expireAfterAccess`这里就是读写多少秒后过期,如果用的是`expireAfterWrite`就是写后的多少秒过期。
