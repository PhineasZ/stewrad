package cc.mousse.steward.server.handler;

import cc.mousse.steward.common.Event;
import cc.mousse.steward.service.GroupService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cc.mousse.steward.common.Constant.*;

/**
 * 普通事件
 *
 * @author PhineasZ
 */
@Slf4j
@Component
public class EventHandler extends SimpleChannelInboundHandler<Event> {

  /** 自定义线程池 */
  private static final ThreadPoolExecutor THREAD_POOL =
      new ThreadPoolExecutor(
          1,
          1,
          0L,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(4),
          r -> {
            val thread = new Thread(r);
            thread.setName("EventHandler");
            return thread;
          });

  @Resource private GroupService groupService;

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Event event) {
    // 忽略自己发言
    if (event.getUserId() != null && Objects.equals(event.getUserId(), event.getSelfId())) {
      return;
    }
    log.debug("Event: {}", event);
    THREAD_POOL.execute(
        () -> {
          switch (event.getPostType()) {
            case NOTICE -> {
              switch (event.getNoticeType()) {
                  // 群名片更新（不保证时效性）
                case "group_card" -> groupService.memberChangeCard(ctx, event);
                  // 群成员减少
                case "group_decrease" -> groupService.memberDecrease(ctx, event);
                  // 群成员增加
                case "group_increase" -> groupService.memberIncrease(ctx, event);
                  // 群消息撤回
                case "group_recall" -> groupService.groupRecall(ctx, event);
                  // 群文件上传
                case "group_upload" -> groupService.groupUpload(ctx, event);
                default -> unknownEvent(event);
              }
            }
            case MESSAGE -> {
              // 群消息
              if (GROUP.equals(event.getMessageType())) {
                groupService.memberMessage(ctx, event);
              }
            }
            case REQUEST -> {
              // 加群请求/邀请
              if (GROUP.equals(event.getRequestType())) {
                groupService.memberJoinRequest(ctx, event);
              }
            }
            default -> unknownEvent(event);
          }
        });
  }

  private void unknownEvent(Event event) {
    log.warn("未添加事件: {}", event);
  }
}
