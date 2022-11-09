package cc.mousse.steward.utils;

import cc.mousse.steward.common.Config;
import cc.mousse.steward.common.CqCode;
import cc.mousse.steward.domain.GroupMember;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static cc.mousse.steward.common.Constant.*;

/**
 * @author PhineasZ
 */
@Slf4j
@Component
@Scope(value = "prototype")
public class ApiUtil {
  @Resource private Config config;

  /**
   * 获取监听群群成员列表
   *
   * @param ctx 上下文环境
   * @param groupId 群号
   * @return 群成员信息列表
   * @throws JsonProcessingException 异常
   * @throws InterruptedException 异常
   */
  public List<GroupMember> getGroupMemberList(ChannelHandlerContext ctx, String groupId)
      throws JsonProcessingException, InterruptedException {
    MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    if (ctx == null) {
      log.warn("ChannelHandlerContext为空");
      return new ArrayList<>();
    }
    val params = new HashMap<String, String>(1);
    params.put(GROUP_ID, groupId);
    val echoId = sendRequest(ctx, new Api("get_group_member_list", params));
    // 转换data字段
    val groupMembers =
        MAPPER.readValue(
            RECEIPT_MAP.get(echoId).getData().toString(), new TypeReference<List<GroupMember>>() {});
    RECEIPT_MAP.remove(echoId);
    return groupMembers;
  }

  /**
   * 获取监听群群成员信息
   *
   * @param ctx 上下文环境
   * @param userId QQ号
   * @return 群成员信息
   * @throws JsonProcessingException 异常
   * @throws InterruptedException 异常
   */
  public GroupMember getGroupMemberInfo(ChannelHandlerContext ctx, String userId)
      throws JsonProcessingException, InterruptedException {
    val params = new HashMap<String, String>(2);
    params.put(GROUP_ID, config.getListenGroupId());
    params.put(USER_ID, userId);
    val echoId = sendRequest(ctx, new Api("get_group_member_info", params));
    // 转换data字段
    val groupMember = MAPPER.readValue(RECEIPT_MAP.get(echoId).getData().toString(), GroupMember.class);
    RECEIPT_MAP.remove(echoId);
    return groupMember;
  }

  /**
   * 获取陌生人（加群申请人）信息
   *
   * @param ctx 上下文环境
   * @param userId QQ号
   * @return 陌生人信息（直接用群成员信息包装）
   * @throws JsonProcessingException 异常
   */
  public GroupMember getStrangerInfo(ChannelHandlerContext ctx, String userId)
      throws JsonProcessingException {
    val params = new HashMap<String, String>(1);
    params.put(USER_ID, userId);
    val echoId = sendRequest(ctx, new Api("get_stranger_info", params));
    // 转换data字段
    val groupMember = MAPPER.readValue(RECEIPT_MAP.get(echoId).getData().toString(), GroupMember.class);
    RECEIPT_MAP.remove(echoId);
    return groupMember;
  }

  /**
   * 设置监听群群成员的群名片（群备注）
   *
   * @param ctx 上下文环境
   * @param userId QQ号
   * @param card 群名片
   * @throws JsonProcessingException 异常
   */
  public void setGroupCard(ChannelHandlerContext ctx, String userId, String card)
      throws JsonProcessingException {
    val params = new HashMap<String, String>(2);
    params.put(GROUP_ID, config.getListenGroupId());
    params.put(USER_ID, userId);
    params.put("card", card);
    sendRequest(ctx, new Api("set_group_card", params));
  }

  /**
   * 处理加群请求／邀请
   *
   * @param ctx 上下文环境
   * @param flag 加群请求的flag（需从上报的数据中获得）
   * @param subType add或invite, 请求类型（需要和上报消息中的sub_type字段相符）
   * @param approve 是否同意请求／邀请
   * @param reason 拒绝理由（仅在拒绝时有效）
   * @throws JsonProcessingException 异常
   */
  public void setGroupAddRequest(
      ChannelHandlerContext ctx, String flag, String subType, Boolean approve, String reason)
      throws JsonProcessingException {
    val params = new HashMap<String, String>(5);
    params.put("flag", flag);
    params.put("sub_type", subType);
    params.put("approve", approve.toString());
    params.put("reason", reason);
    sendRequest(ctx, new Api("set_group_add_request", params));
  }

  /**
   * 发送@Q群管家消息
   *
   * @param ctx 上下文环境
   * @throws JsonProcessingException 异常
   */
  public void atRobot(ChannelHandlerContext ctx) throws JsonProcessingException {
    sendAt(ctx, "", ROBOT_ID);
  }

  /**
   * 发送@信息
   *
   * @param ctx 上下文环境
   * @param message 消息
   * @param userId QQ号
   * @throws JsonProcessingException 异常
   */
  public void sendAt(ChannelHandlerContext ctx, String message, String userId)
      throws JsonProcessingException {
    val atCqCode = CqCode.at();
    atCqCode.getData().put("qq", userId);
    sendGroupMsg(ctx, config.getListenGroupId(), atCqCode + message);
  }

  /**
   * 发送日志消息
   *
   * @param ctx 上下文环境
   * @param message 消息内容
   * @throws JsonProcessingException 异常
   */
  public void sendLog(ChannelHandlerContext ctx, String message) throws JsonProcessingException {
    sendGroupMsg(ctx, config.getReportGroupId(), message);
  }

  /**
   * 发送监听群回复消息
   *
   * @param ctx 上下文环境
   * @param id 消息ID
   * @param message 消息内容
   * @param userId QQ号
   * @throws JsonProcessingException 异常
   */
  public void sendGroupReply(ChannelHandlerContext ctx, String id, String message, String userId)
      throws JsonProcessingException {
    sendGroupReply(ctx, config.getListenGroupId(), id, message, userId);
  }

  /**
   * 发送回复消息
   *
   * @param ctx 上下文环境
   * @param groupId 群号
   * @param id 消息ID
   * @param message 消息内容
   * @param userId QQ号
   * @throws JsonProcessingException 异常
   */
  public void sendGroupReply(
      ChannelHandlerContext ctx, String groupId, String id, String message, String userId)
      throws JsonProcessingException {
    val replyCqCode = CqCode.reply();
    val replyData = replyCqCode.getData();
    replyData.put("id", id);
    val atCqCode = CqCode.at();
    atCqCode.getData().put("qq", userId);
    sendGroupMsg(ctx, groupId, replyCqCode.toString() + atCqCode + message);
  }

  /**
   * 发送群消息
   *
   * @param ctx 上下文环境
   * @param groupId 群号
   * @param message 消息
   * @throws JsonProcessingException 异常
   */
  public void sendGroupMsg(ChannelHandlerContext ctx, String groupId, String message)
      throws JsonProcessingException {
    log.info("发送群消息：{}", message);
    val params = new HashMap<String, String>(1);
    params.put("group_id", groupId);
    params.put("message", message);
    params.put("auto_escape", "false");
    ctx.channel()
        .writeAndFlush(
            new TextWebSocketFrame(MAPPER.writeValueAsString(new Api("send_group_msg", params))));
  }

  /**
   * 统一发送API请求
   *
   * @param ctx 上下文环境
   * @param api API信息
   * @return Echo ID
   * @throws JsonProcessingException 异常
   */
  private String sendRequest(ChannelHandlerContext ctx, Api api) throws JsonProcessingException {
    // 生成EchoId
    val echoId = ECHO_PREFIX + UUID.randomUUID();
    // 补充给bean对象
    api.setEcho(echoId);
    // 发送API
    log.info("发送API: {}", echoId);
    ctx.channel().writeAndFlush(new TextWebSocketFrame(MAPPER.writeValueAsString(api)));
    val startWaitTs = System.currentTimeMillis();
    while (!RECEIPT_MAP.containsKey(echoId)) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        log.error(e.getMessage());
        Thread.currentThread().interrupt();
      }
      if (System.currentTimeMillis() - startWaitTs > 5000) {
        log.error("等待回执超时");
        return null;
      }
    }
    return echoId;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class Api {

    /** 终结点名称，如'send_group_msg' */
    private String action;
    /** 参数 */
    private Map<String, String> params;
    /** '回声'，如果指定了echo字段，那么响应包也会同时包含一个echo字段，它们会有相同的值 */
    private String echo;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Api(String action, Map<String, String> params) {
      this.action = action;
      this.params = params;
    }
  }
}
