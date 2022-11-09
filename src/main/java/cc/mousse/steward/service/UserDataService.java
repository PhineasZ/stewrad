package cc.mousse.steward.service;

import cc.mousse.steward.domain.UserData;

import java.util.List;

/**
 * @author PhineasZ
 */
public interface UserDataService {

  /**
   * 获取所有角色信息
   *
   * @return 角色信息列表
   */
  List<UserData> getAll();

  /**
   * 更新一条角色信息
   *
   * @param userData 角色信息
   */
  void updateOne(UserData userData);

  /**
   * 根据角色名获取角色信息
   *
   * @param name 角色名
   * @return 角色信息
   */
  UserData getOneByName(String name);
}
