package cc.mousse.steward.service.impl;

import cc.mousse.steward.dao.CacheWhitelistDao;
import cc.mousse.steward.domain.CacheWhitelist;
import cc.mousse.steward.service.CacheWhitelistService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author PhineasZ
 */
@Service
@DS("mulitlogin")
public class CacheWhitelistServiceImpl implements CacheWhitelistService {

  @Resource private CacheWhitelistDao cacheWhitelistDao;

  @Override
  public List<CacheWhitelist> getAll() {
    return cacheWhitelistDao.selectList(null);
  }

  @Override
  public CacheWhitelist getOneByName(String name) {
    return cacheWhitelistDao.selectOne(new QueryWrapper<CacheWhitelist>().eq("sign", name));
  }

  @Override
  public void addOne(CacheWhitelist cacheWhitelist) {
    cacheWhitelistDao.insert(cacheWhitelist);
  }

  @Override
  public void deleteOne(CacheWhitelist cacheWhitelist) {
    cacheWhitelistDao.deleteById(cacheWhitelist);
  }
}
