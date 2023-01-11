package cc.mousse.steward.service;

/**
 * @author PhineasZ
 */
public interface CacheService {

  /**
   * 添加一条 QQ号
   *
   * @param userId QQ号
   * @param card 群名片
   */
  void addOne(String userId, String card);

  /**
   * 删除一条 QQ号
   *
   * @param userId userId QQ号
   * @return 群名片
   */
  String deleteOne(String userId);
}
