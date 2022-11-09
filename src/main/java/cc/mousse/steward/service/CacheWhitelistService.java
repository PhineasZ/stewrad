package cc.mousse.steward.service;

import cc.mousse.steward.domain.CacheWhitelist;

import java.util.List;

/**
 * @author PhineasZ
 */
public interface CacheWhitelistService {

  /**
   * 获取所有角色信息
   *
   * @return 角色信息列表
   */
  List<CacheWhitelist> getAll();

  /**
   * 根据角色名获取角色信息
   *
   * @param name 角色名
   * @return 角色信息
   */
  CacheWhitelist getOneByName(String name);

  /**
   * 增加一条角色信息
   *
   * @param cacheWhitelist 角色信息
   */
  void addOne(CacheWhitelist cacheWhitelist);

  /**
   * 删除一条角色信息列表
   *
   * @param cacheWhitelist 角色信息
   */
  void deleteOne(CacheWhitelist cacheWhitelist);
}
