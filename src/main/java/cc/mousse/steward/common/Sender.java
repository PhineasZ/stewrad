package cc.mousse.steward.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author PhineasZ
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Sender {
  String age;
  Object area;
  String card;
  Integer level;
  String nickname;
  String role;
  String sex;
  String title;

  @JsonProperty("group_id")
  String groupId;

  @JsonProperty("user_id")
  String userId;
}
