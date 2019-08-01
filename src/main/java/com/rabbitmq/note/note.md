#关于RabbitMQ

>本项目持续更新 后续会有RabbitMQ的事务，权限管理，以及和springCloudStream集成
***

##首先介绍一下RabbitMQ
RabbitMQ是使用ErLang语言开发的开源消息队列系统，基于AMQP协议来实现。  
AMQP的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）、可靠性、安全。  
AMQP协议更多应用在企业系统内，对数据一致性、稳定性和可靠性要求很高的场景，对性能和吞吐量的要求还在其次  

##一.RabbitMQ的安装
RabbitMQ的安装在这里不多做赘述，先安装ErLang,再安装RabbitMQ即可。百度一下随处可见。推荐安装3.6以上版本。因为有些插件3.6以上才支持  

##二.RabbitMQ基本组件和概念
+ Producer `消息生产者`  
+ Exchange `交换机:它指定消息按什么规则，路由到哪个队列`  
+ Queue `队列:消息队列载体，每个消息都会被投入到一个或多个队列`  
+ Consumer `消息消费者 ps:多个监听一个队列的消费者每次只有一个消费者可以收到消息`  
  
- Binding `绑定:消息队列与交换器直接关联的，它的作用就是把Exchange和Queue按照路由规则绑定起来`
- Routing Key `路由键:路由关键字，Exchange根据这个关键字进行消息投递`
- Virtual Host `虚拟主机:同时一个Broker里可以开设多个vhost，用作不同用户的权限分离`  

总的结构图如下所示:  
![AMQP](https://img-blog.csdn.net/20180421155055731?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0FudW1icmVsbGE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)  
###1.交换机
四种交换机:  
+ Direct exchange
+ Fanout exchange
+ Topic  exchange
+ Headers exchange  
声明durable属性可以使交换机持久化  
1.Direct exchange  
直连交换机是根据消息携带的路由键将消息投递给对应的队列的。也就是发送消息时会携带Routing Key，交换机会根据Routing Key来找对应的队列，找到后推送到对应的队列中  
这里的匹配就是完全匹配，这个模式就是直连交换机  
![Direct exchange](https://img-blog.csdn.net/20180510231113379?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0FudW1icmVsbGE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
2.Fanout exchange  
扇形交换机会将消息路由给绑定到它身上的所有队列，而不理会绑定的路由键  
如果N个队列绑定到某个扇型交换机上，当有消息发送给此扇型交换机时,交换机会将消息的拷贝分别发送给这所有的N个队列,所以扇形交换机主要做的就是广播消息  
eg:分发系统使用它来广播各种状态和配置更新  
![Direct exchange](https://img-blog.csdn.net/2018051122381940?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0FudW1icmVsbGE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)  
生产者发送一个消息给扇形交换机，扇形交换机会把消息投递给所有绑定了它的队列，而这个与routing_key（路由键）无关，它采用广播机制传递消息  
3.Topic  exchange  
主题交换机通过对消息的路由键和队列到交换机的绑定模式之间的匹配，将消息路由给一个或多个队列  
主题交换机非常灵活，它是通过使用带规则的routing_key来对实现对消费分配到队列中，可以实现一个消息发送给一个或多个队列中  
主题交换机的routing_key需要有一定的规则，采用.#.…..的格式，每个部分用.分开  
假设有一条消息的routing_key为net.rabbit.kk，那么带有这样routing_key的几个队列都会接收这条消息：  
1、net.*.*  
2、*.*.kk  
3、net.#  
eg:分发有关于特定地理位置的数据，例如销售点  
![Topic  exchange](https://img-blog.csdn.net/20180511231453234?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0FudW1icmVsbGE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)  
4.Headers exchange  
有时消息的路由操作会涉及到多个属性，此时使用消息头就比用路由键更容易表达  
头交换机使用多个消息属性来代替路由键建立路由规则。通过判断消息头的值能否与指定的绑定相匹配来确立路由规则  
相比较于直达交换机，头交换机的优势是匹配的规则不被限定为字符串，头交换机需要在队列绑定的规则中指定消息头和匹配的规则  
匹配规则x-match有下列两种类型：  
+ x-match = all ：表示所有的键值对都匹配才能接受到消息
+ x-match = any ：表示只要有键值对匹配就能接受到消息  
就是在指定消息头（键值对）时，添加”x-match”参数，当”x-match”设置为“any”时，消息头的任意一个值被匹配就可以满足条件，而当”x-match”设置为“all”的时候，就需要消息头的所有值都匹配成功  
不同之处在于头交换机的路由规则是建立在头属性值之上，而不是路由键。路由键必须是一个字符串，而头属性值则没有这个约束，它们甚至可以是整数或者哈希值（字典）等  
生产者发送一个含有消息头为 {“key2”：“value2”} 的消息，到头交换机后，交换机会根据队列绑定消息头中“x-match”的匹配规则（就是上面介绍的all、any的规则），把消息投递给满足消息头匹配规则的队列中，然后再投递给监听该队列的用户  
这里的匹配就是消息头（键值对）模式，这个模式就是头交换机  
![Headers exchange](https://img-blog.csdn.net/20180512151555970?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0FudW1icmVsbGE=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)  
  
总结：  
**直达交换机**：先匹配，再投递，即在绑定时设定一个routing_key，消息的routing_key完全匹配时，才会被交换机投递到绑定的队列中。 
**扇形交换机**：与routing_key无关，把所有消息投递给所有绑定的队列中。 
**主题交换机**：绑定routing_key，在匹配routing_key时按照正则配置的规则投递消息到队列中，这个也是最灵活的交换机。 
**头交换机**：使用多个消息属性来替代路由键建立路由规则，可以实现部分匹配或全部匹配。
##三.RabbitMQ基本工作流程
1.声明交换机和队列，并根据交换机类型和交换机以及队列的属性各自绑定好  
2.Producer发送消息，消息发到指定交换机中  
3.指定交换机收到消息后，查找绑定，然后根据规则和路由键(Routing Key)分发到队列中  
4.Consumer监听对应的队列(Queue)，一旦队列中有可消费的消息，队列就会将消息发给监听者  
5.**消息确认，当Consumer完成一条消息的处理后，需要发送一条ACK消息给对应的队列**  
Consumer收到消息时需要显式的向RabbitMQ Broker发送basic.ack消息或者Consumer订阅消息时设置auto_ack参数为true。 
  
在通信过程中，队列对ACK的处理有以下几种情况：  
+ 如果Consumer接收了消息，发送ack，RabbitMQ会删除队列中这个消息，发送另一条消息给Consumer
+ 如果Consumer接收了消息, 但如果没有发送ACK，那么这条消息会去到RabbitMQ的UnAckEd下面。但不会重复推送消息。这时如果关闭Consumer那么消息会  
  回到ready下面。重启Consumer时会再次推送消息。以此类推，直到确认为止。这样就可以避免因为Consumer挂掉而导致的消息丢失  
  
##四.RabbitMQ延迟消息插件

###1.插件安装  
<a href="https://www.rabbitmq.com/blog/2015/04/16/scheduling-messages-with-rabbitmq/" target="_blank">官方说明链接</a>  
1.插件下载，找到rabbitmq_delayed_message_exchange插件，根据您使用的RabbitMQ版本选择对应的插件版本下载即可  
<a href="https://www.rabbitmq.com/community-plugins.html" target="_blank">下载地址</a>  
2.下载好之后，解压得到.ez结尾的插件包，将其复制到RabbitMQ安装目录下的plugins文件夹  
3.通过命令行启用该插件：
```shell
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```
该插件在通过上述命令启用后就可以直接使用，不需要重启  
以下是发送代码。具体配置在config中查看
```java
rabbitTemplate.convertAndSend("DELAY_EXCHANGE", "DELAY_ROUTING_KEY", msg, (message) ->{
            message.getMessageProperties().setHeader("x-delay", 9000); 
            return message;
        });
```
 


