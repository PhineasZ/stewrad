package cc.mousse.steward.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 常量信息
 *
 * @author PhineasZ
 */
public class Constant {

  public static final Map<String, Receipt> RECEIPT_MAP = new ConcurrentHashMap<>();
  public static final ObjectMapper MAPPER = new ObjectMapper();
  public static final String ADD = "add";
  public static final String NOTICE = "notice";
  public static final String MESSAGE = "message";
  public static final String REQUEST = "request";
  public static final String GROUP = "group";
  public static final String GROUP_ID = "group_id";

  public static final String USER_ID = "user_id";

  public static final String ROBOT_ID = "2854196310";

  public static final String LIFE_CYCLE = "lifecycle";
  /** 心跳 */
  public static final String HEART_BEAT = "heartbeat";
  /** 白名单 */
  public static final String WHITELIST = "白名单";

  public static final String RETURN_CODE = "retcode";

  public static final String ECHO_PREFIX = "ECHO--";

  private Constant() {}
}
