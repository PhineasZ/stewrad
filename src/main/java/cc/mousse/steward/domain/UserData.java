package cc.mousse.steward.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PhineasZ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "multilogin.multilogin_user_data")
public class UserData {

  @TableId private Byte[] onlineUuid;
  private String currentName;
  private Byte[] redirectUuid;
  private String yggdrasilService;
  private Integer whitelist;
}
