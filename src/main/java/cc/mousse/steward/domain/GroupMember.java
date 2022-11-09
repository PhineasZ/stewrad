package cc.mousse.steward.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author PhineasZ
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {

  /** 地区 */
  private String area;

  /** 最后发言时间戳 */
  @JsonProperty("last_sent_time")
  private Date lastSentTime;

  /** 是否不良记录成员 */
  private Boolean unfriendly;
  /** 角色，owner或admin或member */
  private Role role;

  /** 成员等级 */
  private Integer level;
  /** 性别 */
  private Sex sex;

  /** 专属头衔过期时间戳 */
  @JsonProperty("title_expire_time")
  private Date titleExpireTime;

  /** 是否允许修改群名片 */
  @JsonProperty("card_changeable")
  private Boolean cardChangeable;

  /** 专属头衔 */
  private String title;

  /** 加群时间戳 */
  @JsonProperty("join_time")
  private Date joinTime;

  /** 群号 */
  @JsonProperty("group_id")
  private String groupId;

  /** QQ号 */
  @JsonProperty("user_id")
  private String userId;

  /** 昵称 */
  @JsonProperty("nickname")
  private String nickName;

  /** 禁言到期时间 */
  @JsonProperty("shut_up_timestamp")
  private Date shutUpTimestamp;

  /** 群名片/备注 */
  private String card;

  /** 年龄 */
  private Integer age;

  /** QID身份卡 */
  private String qid;

  /** 等级 */
  @JsonProperty("login_days")
  private String loginDays;

  enum Role {
    /** 角色，owner或admin或member */
    @JsonProperty("owner")
    OWNER,
    @JsonProperty("admin")
    ADMIN,
    @JsonProperty("member")
    MEMBER
  }

  enum Sex {
    /** 性别，male或female或unknown */
    @JsonProperty("male")
    MALE,
    @JsonProperty("female")
    FEMALE,
    @JsonProperty("unknown")
    UNKNOWN
  }
}
