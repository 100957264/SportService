package com.yt.sportservice.step;

import android.app.AlarmManager;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;


import com.yt.sportservice.dao.StepHistoryDao;
import com.yt.sportservice.entity.StepHistory;
import com.yt.sportservice.manager.DBManager;
import com.yt.sportservice.manager.GreenDaoChargeRecordImpl;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.util.ListUtils;
import com.yt.sportservice.util.LogUtils;

import org.greenrobot.greendao.query.Query;

import java.util.Date;
import java.util.List;

/**
 * @author mare
 * @Description:TODO 历史计步记录
 * @csdnblog http://blog.csdn.net/mare_blue * @date 2017/9/5
 * @time 18:08
 */
public class StepHistoryDaoUtils implements GreenDaoChargeRecordImpl<StepHistory> {

    private static final int DATA_RESERVE_DAY = 7;//默认只保留7天数据

    private StepHistoryDaoUtils() {
    }

    private static class SingletonHolder {
        private static final StepHistoryDaoUtils INSTANCE = new StepHistoryDaoUtils();
    }

    public static StepHistoryDaoUtils instance() {
        return SingletonHolder.INSTANCE;
    }

    private StepHistoryDao stepHistoryDao;

    private StepHistoryDao getDao() {
        if (null == stepHistoryDao) {
            stepHistoryDao = DBManager.instance().getDaoSession().getStepHistoryDao();
        }
        return stepHistoryDao;
    }

    @Override
    public long insert(StepHistory data) {
        return getDao().insert(data);
    }

    /***
     * TODO 更新计步信息
     * @param data 当次计步信息
     * @return 是否更新成功
     */
    @Override
    public boolean update(final StepHistory data) {
        final StepHistoryDao dao = getDao();
        dao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                if (null == data) {
                    return;
                }
                String imei =StaticManager.IMEI;
                if (TextUtils.isEmpty(imei)) {
                    return;
                }
                //deleteOldData(imei);//删除旧数据
                StepHistory userQuery = null;
                long insertUsersID;
                String className = data.getClass().getSimpleName();
                Query<StepHistory> query;
                long time = data.getTime();
                LogUtils.d("update Step Data " + data.toString());
                try {
                    query = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei),
                            StepHistoryDao.Properties.Time.eq(time)).build();
                    userQuery = query.forCurrentThread().unique();
                } catch (SQLiteException e) {
                    LogUtils.e("查询 " + className + "失败");
                }
                if (null != userQuery) {//覆盖更新闹钟
                    data.setId(userQuery.getId());
                    LogUtils.d("开始更新" + className + "数据...");
                } else {
                    data.setId(System.currentTimeMillis());
                    LogUtils.d("没找到" + className + "的所在数据 开始插入");
                }
                insertUsersID = dao.insertOrReplace(data);
                boolean isSuccess = insertUsersID >= 0;
//                LogUtils.i("更新 StepHistory " + "的计步数据" + DaoFlagUtils.insertSuccessOr(isSuccess));
            }
        });
        return true;
    }

    /**
     * TODO 批量插入步数
     *
     * @param stepHistories 多条计步信息
     */
    public void updateStepHistoryList(final List<StepHistory> stepHistories) {
        final StepHistoryDao dao = getDao();
        if (null == stepHistories || stepHistories.isEmpty()) return;
        dao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < stepHistories.size(); i++) {
                    StepHistory stepHistory = stepHistories.get(i);
                    String imei = stepHistory.getImei();
                    long time = stepHistory.getTime();
                    StepHistory queryInfo;
                    queryInfo = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei),
                            StepHistoryDao.Properties.Time.eq(time)).build().unique();
                    if (null != queryInfo) {
                        stepHistory.setId(queryInfo.getId());
                    } else {
                        stepHistory.setId(System.currentTimeMillis());//以当前时间戳作为id
                    }
                    long insertUsersID = dao.insertOrReplace(stepHistory);
                    boolean isSuccess = insertUsersID >= 0;
//                    LogUtils.i("更新 id= " + i + " 的数据" + DaoFlagUtils.insertSuccessOr(isSuccess));
                }
            }
        });
    }

    /**
     * TODO 清除某天的记录
     *
     * @param date
     */
    public void clearStepHistory(final String date) {
        final StepHistoryDao dao = getDao();
        final String imei = "";
        dao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                List<StepHistory> stepHistories = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei),
                        StepHistoryDao.Properties.Date.eq(date)).build().forCurrentThread().list();
                if (!ListUtils.isEmpty(stepHistories)) {
                    for (StepHistory history : stepHistories) {
                        dao.delete(history);
                    }
                    LogUtils.i("clearStepHistory completed !!!");
                }
            }
        });
    }

    /**
     * TODO 清除某天的记录
     *
     * @param time 某个时间点的计步数据
     */
    public void clearStepHistory(final long time) {
        final StepHistoryDao dao = getDao();
        final String imei =StaticManager.IMEI ;
        dao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                List<StepHistory> stepHistories = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei),
                        StepHistoryDao.Properties.Time.eq(time)).build().forCurrentThread().list();
                if (!ListUtils.isEmpty(stepHistories)) {
                    for (StepHistory history : stepHistories) {
                        dao.delete(history);
                    }
                    LogUtils.i("clearStepHistory completed !!!");
                }
            }
        });
    }

    /**
     * TODO 获取最新的计步数据
     *
     * @return
     */
    public StepHistory queryLatestHistory() {
        StepHistory result = null;
        String imei = StaticManager.IMEI ;
        if (TextUtils.isEmpty(imei)) {
            return null;
        }
        StepHistoryDao dao = getDao();
        try {
            result = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei))
                    .orderDesc(StepHistoryDao.Properties.Id).limit(1).unique();
        } catch (Exception e) {
            LogUtils.e("查询 " + dao.getClass().getSimpleName() + "失败");
        } finally {
            return result;
        }
    }

    /**
     * @return 返回最新的数据
     */
    public StepHistory queryLatestHistoryByNow() {
        String date = StepTimeUtil.getDateFormat().format(new Date());
        return queryLatestHistoryByDate(date);
    }

    /**
     * @return 返回最新的数据
     */
    public List<StepHistory> queryHistorysByDate() {
        String date = StepTimeUtil.getDateFormat().format(new Date());
        return queryHistorysByDate(date);
    }

    /**
     * @param date 180117 :2018年01月17日
     * @return 返回特定日期下的所有计步数据
     */
    public List<StepHistory> queryHistorysByDate(String date) {
        List<StepHistory> result = null;
        String imei = StaticManager.IMEI;
        if (TextUtils.isEmpty(imei)) {
            return null;
        }
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        StepHistoryDao dao = getDao();
        try {
            result = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei)
                    , StepHistoryDao.Properties.Date.eq(date))
                    .orderDesc(StepHistoryDao.Properties.Id).list();
        } catch (Exception e) {
            LogUtils.e("查询 " + dao.getClass().getSimpleName() + "失败");
        } finally {
            return result;
        }
    }

    /**
     * @param date 180117 :2018年01月17日
     * @return 返回最新的数据
     */
    public StepHistory queryLatestHistoryByDate(String date) {
        StepHistory result = null;
        String imei = StaticManager.IMEI ;
        if (TextUtils.isEmpty(imei)) {
            return null;
        }
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        StepHistoryDao dao = getDao();
        try {
            result = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei)
                    , StepHistoryDao.Properties.Date.eq(date))
                    .orderDesc(StepHistoryDao.Properties.Id).limit(1).unique();
        } catch (Exception e) {
            LogUtils.e("查询 " + dao.getClass().getSimpleName() + "失败");
        } finally {
            return result;
        }
    }

    /**
     * @param date 要查询的日期
     * @param time 离要查询的时间
     * @return 返回特定日期下离目标时间最近的最新记录
     */
    public StepHistory queryLatestHistoryByDateTime(String date, String time) {
        StepHistory result = null;
        String imei =StaticManager.IMEI ;
        if (TextUtils.isEmpty(imei)) {
            return null;
        }
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        if (TextUtils.isEmpty(time)) {
            return null;
        }
        StepHistoryDao dao = getDao();
        try {
            Date targetDate = StepTimeUtil.getDateTimeFormat().parse(StepTimeUtil.formatDateTime(date, time));
            long targetMillions = targetDate.getTime();
            result = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei)
                    , StepHistoryDao.Properties.Date.eq(date), StepHistoryDao.Properties.Time.le(targetMillions))
                    .orderDesc(StepHistoryDao.Properties.Id).limit(1).unique();
        } catch (Exception e) {
            LogUtils.e("查询 " + dao.getClass().getSimpleName() + "失败");
        } finally {
            return result;
        }
    }

    /**
     * @param imei TODO 删除最前几天的计步数据
     */
    public void deleteOldData(final String imei) {
        final StepHistoryDao dao = getDao();
        try {
            dao.getSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    long targetMillions = System.currentTimeMillis() - AlarmManager.INTERVAL_DAY * DATA_RESERVE_DAY;
                    final List<StepHistory> result = dao.queryBuilder().where(StepHistoryDao.Properties.Imei.eq(imei)
                            , StepHistoryDao.Properties.Time.le(targetMillions))
                            .orderDesc(StepHistoryDao.Properties.Id).list();
                    if (null == result) {
                        return;
                    }
                    for (StepHistory history : result) {
                        if (null == history) {
                            continue;
                        }
                        dao.delete(history);
                    }
                }
            });
        } catch (Exception e) {
            LogUtils.e("删除旧数据 " + dao.getClass().getSimpleName() + "失败");
        }
    }

    @Override
    public void deleteAll() {
        getDao().deleteAll();
    }

    @Override
    public void deleteWhere(long id) {
        getDao().deleteByKey(id);
    }

    @Override
    public List<StepHistory> selectAll() {
        return getDao().loadAll();
    }

    @Override
    public List<StepHistory> selectWhere(StepHistory data) {
        return null;
    }

    @Override
    public StepHistory seelctWhrer(String name) {
        return null;
    }

    @Override
    public List<StepHistory> selectWhrer(long id) {
        return null;
    }
}
