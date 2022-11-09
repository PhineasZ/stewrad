package cc.mousse.steward.service;

import cc.mousse.steward.common.Event;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author PhineasZ
 */
public interface GroupService {

  /**
   * 群成员减少事件
   *
   * @param ctx 上下文环境
   * @param event 事件对象
   */
  void memberDecrease(ChannelHandlerContext ctx, Event event);

  /**
   * 群成员增加事件
   *
   * @param ctx 上下文环境
   * @param event 事件对象
   */
  void memberIncrease(ChannelHandlerContext ctx, Event event);
  /**
   * 群成员发送消息事件
   *
   * @param ctx 上下文环境
   * @param event 事件对象
   */
  void memberMessage(ChannelHandlerContext ctx, Event event);
  /**
   * 群成员修改群名片事件
   *
   * @param ctx 上下文环境
   * @param event 事件对象
   */
  void memberChangeCard(ChannelHandlerContext ctx, Event event);
  /**
   * 群成员入群申请
   *
   * @param ctx 上下文环境
   * @param event 事件对象
   */
  void memberJoinRequest(ChannelHandlerContext ctx, Event event);
}
