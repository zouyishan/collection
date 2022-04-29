# Docker的安装配置

## Docker的基本组成

**Docker的架构图**

![image-20210418104728067](https://gitee.com/rem01/images/raw/master/img/20210418104739.png)

**镜像(image):**

Docker镜像(Image)就是一个只读的模板。镜像可以用来创建Docker容器，一个镜像可以创建很多容器。就好似Java中的类和对象，类就是镜像，容器就是对象！



**容器(container):**

Docker利用容器(Container)独立运行的一个或一组应用。容器是用镜像创建的运行实例。

它可以被启动，开始，停止，删除。每个容器都是相互隔离的，保证安全的平台。

可以把容器看做是一个简易版的Linux环境(包括root用户权限，进程空间，用户空间和网络空间等)和运行在其中的应用程序。

容器的定义和镜像几乎一摸一样，也是一堆层的统一视角，唯一区别在于容器的最上面那一层是可读可写的。



**仓库(repository):**

仓库(repository)是集中存放镜像文件的场所。

仓库和仓库注册服务器是有区别的。仓库注册服务器上往往存放着多个仓库，每个仓库中又包含了多个镜像，每个镜像有不同的标签

仓库分为公开仓库和私有仓库两种形式。

最大的公开仓库是Docker Hub (https://hub.docker.com) ,存放了数量庞大的镜像供用户下载。

国内的公开仓库包括阿里云，网抑云等



**小结:**

需要正确的理解仓库/镜像/容器这几个概念：

* Docker本身是一个容器运行载体或称之为管理引擎。我们把应用程序和配置依赖打包好形成一个可交付的运行环境，这个打包好的运行环境就是image镜像文件。只有通过这个镜像文件才能生成Docker容器。image文件可以看作是容器的模板。Docker根据image文件生成容器的实例。同一个image文件，可以生成多个同时运行的容器实例。
* image文件生成的容器实例，本身也是一个文件，称为镜像文件。
* 一个容器运行一种服务，当我们需要的时候，就可以通过docekr客户端创建一个对应的运行实例，也就是我们的容器。
* 至于仓库，就是放了一堆镜像的地方，我们可以把镜像发布到仓库中，需要的时候从仓库中拉下来就可以了。



## 环境说明

我们使用的是CentOS 7(64-bit)

目前，CentOS仅发行版本中的内核支持Docker。

Docker运行在CentOS 7上，要求系统为64位，系统内核版本位3.10以上

**查看自己的内核:**

`uname -r`命令用于打印当前系统相关信息(内核版本号，硬件架构，主机名称和操作系统类型等)

```bash
[root@izbp18zf5g3idsy51nt0ziz etc]# uname -r
3.10.0-514.26.2.el7.x86_64
```

**查看版本信息:**

```bash
[root@izbp18zf5g3idsy51nt0ziz etc]# cat /etc/os-release
NAME="CentOS Linux"
VERSION="7 (Core)"
ID="centos"
ID_LIKE="rhel fedora"
VERSION_ID="7"
PRETTY_NAME="CentOS Linux 7 (Core)"
ANSI_COLOR="0;31"
CPE_NAME="cpe:/o:centos:centos:7"
HOME_URL="https://www.centos.org/"
BUG_REPORT_URL="https://bugs.centos.org/"

CENTOS_MANTISBT_PROJECT="CentOS-7"
CENTOS_MANTISBT_PROJECT_VERSION="7"
REDHAT_SUPPORT_PRODUCT="centos"
REDHAT_SUPPORT_PRODUCT_VERSION="7"
```



## 安装步骤

1. 官网安装参考手册：https://docs.docker.com/engine/install/centos/

2. 确定你的CentOS7及以上版本，我们已经做过了

3. yum安装gcc相关环境（需要确保虚拟机可以上网）

   ```bash
   yum -y install gcc
   yum -y install gcc-c++
   ```

4. 卸载旧版本

   ```bash
   sudo yum remove docker \
                     docker-client \
                     docker-client-latest \
                     docker-common \
                     docker-latest \
                     docker-latest-logrotate \
                     docker-logrotate \
                     docker-engine
   ```

5. 安装需要的软件包

   ```bash
   yum install -y yum-utils
   ```

6. 设置镜像仓库

   ```bash
   # 推荐使用国内的
   yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
   ```

7. 更新yum软件包索引

   ```bash
   yum makecache fast
   ```

8. 安装Docker CE

   ```bash
   yum install docker-ce docker-ce-cli containerd.io
   ```

9. 启动Docker

   ```bash
   systemctl start docker
   ```

10. 测试命令

    ```bash
    docker version
    docker run hello-world
    docker images
    ```

    ![image](https://user-images.githubusercontent.com/57765968/115145327-8b1c9c00-a083-11eb-8b23-bf1aaac78bbb.png)

11. 卸载

    ```bash
    systemctl stop docker
    
    yum -y remove docker-ce docker-ce-cli containerd.io
    
    rm -rf /var/lib/docker
    ```

    

## 阿里云镜像加速

1. 介绍：https://www.aliyun.com/product/acr
2. 注册一个属于自己的阿里云账户(可复用淘宝账号)
3. 进入管理控制台设置密码，开通
4. 查看自己的镜像加速器.


 ![image-20210418122718915](https://gitee.com/rem01/images/raw/master/img/20210418122800.png)

好了 到这里基本的docker就配置完了！
