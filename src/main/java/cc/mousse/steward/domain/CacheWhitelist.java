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
@TableName(value = "multilogin.multilogin_cache_whitelist")
public class CacheWhitelist {

  @TableId private String sign;
}
