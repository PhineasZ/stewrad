package cc.mousse.steward.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author PhineasZ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "blessingskin.players")
public class Player {

  @TableId private Integer pid;
  private Integer uid;
  private String name;
  private Integer tidCape;
  private Date lastModified;
  private Integer tidSkin;
}
