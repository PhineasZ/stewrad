package cc.mousse.steward.service.impl;

import cc.mousse.steward.dao.UserDataDao;
import cc.mousse.steward.domain.UserData;
import cc.mousse.steward.service.UserDataService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author PhineasZ
 */
@Service
@DS("multilogin")
public class UserDataServiceImpl implements UserDataService {

  @Resource private UserDataDao userDataDao;

  @Override
  public List<UserData> getAll() {
    return userDataDao.selectList(null);
  }

  @Override
  public void updateOne(UserData userData) {
    userDataDao.updateById(userData);
  }

  @Override
  public UserData getOneByName(String name) {
    return userDataDao.selectOne(new QueryWrapper<UserData>().eq("current_name", name));
  }
}
