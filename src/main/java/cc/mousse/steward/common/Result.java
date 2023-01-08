package cc.mousse.steward.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 白名单设置结果
 *
 * @author PhineasZ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

  private Status status;
  private Cause cause;
  private Set<String> data;

  public static Result success(String name) {
    var set = new HashSet<String>();
    set.add(name);
    return new Result(Status.SUCCESS, Cause.SUCCESS, set);
  }

  public static Result fail(String name) {
    var set = new HashSet<String>();
    set.add(name);
    return new Result(Status.FAIL, Cause.FAIL, set);
  }

  public static Result playerNotFound(String name) {
    var set = new HashSet<String>();
    set.add(name);
    return new Result(Status.FAIL, Cause.PLAYER_NOT_FOUND, set);
  }

  public static Result whitelistAlreadyExists(String name) {
    var set = new HashSet<String>();
    set.add(name);
    return new Result(Status.FAIL, Cause.WHITELIST_ALREADY_EXISTS, set);
  }

  public static Result noLegalName() {
    return new Result(Status.FAIL, Cause.NO_LEGAL_NAME, null);
  }

  public static Result outOfLimit(String name) {
    var set = new HashSet<String>();
    set.add(name);
    return new Result(Status.FAIL, Cause.OUT_OF_LIMIT, set);
  }

  public void appendData(String data) {
    this.data.add(data);
  }

  public enum Status {
    /**
     * SUCCESS：添加成功<br>
     * FAIL：添加失败
     */
    SUCCESS,
    FAIL
  }

  public enum Cause {
    /**
     * SUCCESS：添加成功<br>
     * FAIL：添加失败<br>
     * PLAYER_NOT_FOUND：BlessingSkin找不到玩家<br>
     * WHITELIST_ALREADY_EXISTS：白名单已经存在<br>
     * NO_LEGAL_NAME：没有合规角色名<br>
     * OUT_OF_LIMIT：超出数量限制
     */
    SUCCESS,
    FAIL,
    PLAYER_NOT_FOUND,
    WHITELIST_ALREADY_EXISTS,
    NO_LEGAL_NAME,
    OUT_OF_LIMIT
  }
}
