package cc.mousse.steward.server;

import cc.mousse.steward.common.Config;
import cc.mousse.steward.common.Event;
import cc.mousse.steward.common.Receipt;
import cc.mousse.steward.server.handler.EventHandler;
import cc.mousse.steward.server.handler.ReceiptHandler;
import cc.mousse.steward.utils.ApiUtil;
import cc.mousse.steward.utils.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Objects;

import static cc.mousse.steward.common.Constant.*;

/**
 * 消息入口
 *
 * @author PhineasZ
 */
@Slf4j
@Service
public class Gateway {

  @Resource private Config config;
  @Resource private ApiUtil apiUtil;

  @Resource private StrUtil strUtil;
  @Resource private EventHandler eventHandler;
  @Resource private ReceiptHandler receiptHandler;

  public void start() {
    val mapper = new ObjectMapper();
    val leader = new NioEventLoopGroup();
    val worker = new NioEventLoopGroup();
    new ServerBootstrap()
        .group(leader, worker)
        .channel(NioServerSocketChannel.class)
        .childHandler(
            new ChannelInitializer<NioSocketChannel>() {
              @Override
              protected void initChannel(NioSocketChannel nioSocketChannel) {
                nioSocketChannel
                    .pipeline()
                    // HTTP协议解析，用于握手阶段
                    .addLast(new HttpServerCodec())
                    // 分块向客户端写数据，防止发送大文件时导致内存溢出，channel.write(new ChunkedFile(new
                    // File("bigFile.mkv")))
                    .addLast(new ChunkedWriteHandler())
                    // HTTP协议解析，用于握手阶段
                    .addLast(new HttpObjectAggregator(65535))
                    // 聚合 websocket 的数据帧，因为客户端可能分段向服务器端发送数据
                    .addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE))
                    // WebSocket数据压缩扩展
                    .addLast(new WebSocketServerCompressionHandler())
                    // WebSocket握手、控制帧处理
                    .addLast(new WebSocketServerProtocolHandler("/"))
                    .addLast(
                        new SimpleChannelInboundHandler<TextWebSocketFrame>() {
                          @Override
                          protected void channelRead0(
                              ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame)
                              throws Exception {
                            val text = textWebSocketFrame.text();
                            // 判断事件类型
                            val jsonNode = mapper.readTree(text);
                            val eventType = jsonNode.get("meta_event_type");
                            if (eventType != null) {
                              val metaEventType = strUtil.removeQuotes(eventType.toString());
                              if (Objects.equals(LIFE_CYCLE, metaEventType)) {
                                val message = "前后端已同步";
                                log.info(message);
                                apiUtil.sendLog(ctx, message);
                                return;
                              }
                              // 忽略心跳
                              if (HEART_BEAT.equals(metaEventType)) {
                                return;
                              }
                            }
                            // 判断消息类型
                            if (jsonNode.get(RETURN_CODE) != null) {
                              // 消息回执
                              try {
                                val receipt = mapper.readValue(text, Receipt.class);
                                super.channelRead(ctx, receipt);
                              } catch (Exception e) {
                                log.error(e.getMessage());
                              }
                            } else {
                              // 事件消息
                              try{
                                val event = mapper.readValue(text, Event.class);
                                super.channelRead(ctx, event);
                              } catch (Exception e) {
                                log.error(e.getMessage());
                              }

                            }
                          }
                        })
                    .addLast(eventHandler)
                    .addLast(receiptHandler);
              }
            })
        .bind(config.getServerPort());
    log.info("后端已启动");
  }
}
