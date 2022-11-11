package cc.mousse.steward.common;

import cc.mousse.steward.domain.GroupMember;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;


/**
 * 事件消息
 *
 * @author PhineasZ
 */
@Data
@ToString
public class Event {

  /** post_type为meta_event的上报会有以下有效数据 */
  @JsonProperty("meta_event_type")
  private String metaEventType;
  /** 消息序号 */
  @JsonProperty("message_seq")
  String messageSeq;
  /** 收到事件的机器人的QQ号 */
  @JsonProperty("self_id")
  private String selfId;

  @JsonProperty("self_tiny_id")
  private String selfTinyId;
  /** 事件发生的时间戳 */
  private Long time;
  /** 表示该上报的类型，消息，请求，通知或元事件 */
  @JsonProperty("post_type")
  private String postType;
  /** 消息类型 */
  @JsonProperty("message_type")
  private String messageType;
  /** 表示消息的子类型 */
  @JsonProperty("sub_type")
  private String subType;
  /** 消息ID */
  @JsonProperty("message_id")
  private Long messageId;
  /** 发送者QQ号 */
  @JsonProperty("user_id")
  private String userId;
  /** 一个消息链 */
  private String message;
  /** CQ码格式的消息 */
  @JsonProperty("raw_message")
  private String rawMessage;
  /** 字体 */
  private Integer font;
  /** 发送者信息 */
  private Sender sender;
  /** 操作者QQ号（如果是主动退群，则和user_id相同） */
  @JsonProperty("operator_id")
  private String operatorId;
  /** 验证信息 */
  private String comment;
  /** 请求flag，在调用处理请求的API时需要传入 */
  private String flag;
  /** 群成员/陌生人信息 */
  private GroupMember groupMember;
  /** 临时会话来源 */
  @JsonProperty("temp_source")
  private String tempSource;
  /** 群号 */
  @JsonProperty("group_id")
  private String groupId;
  /** 戳一戳发送者QQ号 */
  @JsonProperty("sender_id")
  private String senderId;
  /** 戳一戳被戳者QQ号 */
  @JsonProperty("target_id")
  private String targetId;
  /** 匿名信息，如果不是匿名消息则为null */
  private String anonymous;
  /** 新名片 */
  @JsonProperty("card_new")
  private String cardNew;
  /** 旧名片 */
  @JsonProperty("card_old")
  private String cardOld;
  /** 请求类型 */
  @JsonProperty("request_type")
  private String requestType;
  /** 通知类型 */
  @JsonProperty("notice_type")
  private String noticeType;
  /** 荣誉类型 */
  @JsonProperty("honor_type")
  private String honorType;
  /** 文件信息 */
  private File file;
}
