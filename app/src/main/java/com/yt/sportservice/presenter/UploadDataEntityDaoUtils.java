package com.yt.sportservice.presenter;

import android.app.AlarmManager;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.yt.sportservice.dao.UploadDataEntityDao;
import com.yt.sportservice.entity.StepHistory;
import com.yt.sportservice.entity.UploadDataEntity;
import com.yt.sportservice.manager.DBManager;
import com.yt.sportservice.manager.GreenDaoChargeRecordImpl;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.step.StepTimeUtil;
import com.yt.sportservice.util.ListUtils;
import com.yt.sportservice.util.LogUtils;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.Query;

import java.util.Date;
import java.util.List;

/**
 * @author mare
 * @Description:TODO 历史计步记录
 * @csdnblog http://blog.csdn.net/mare_blue * @date 2017/9/5
 * @time 18:08
 */
public class UploadDataEntityDaoUtils implements GreenDaoChargeRecordImpl<UploadDataEntity> {


    private UploadDataEntityDaoUtils() {
    }

    private static class SingletonHolder {
        private static final UploadDataEntityDaoUtils INSTANCE = new UploadDataEntityDaoUtils();
    }

    public static UploadDataEntityDaoUtils instance() {
        return SingletonHolder.INSTANCE;
    }

    private UploadDataEntityDao uploadDataEntityDao;

    private UploadDataEntityDao getDao() {
        if (null == uploadDataEntityDao) {
            uploadDataEntityDao = DBManager.instance().getDaoSession().getUploadDataEntityDao();
        }
        return uploadDataEntityDao;
    }

    @Override
    public long insert(UploadDataEntity data) {
        return getDao().insert(data);
    }

    /***
     * TODO 更新计步信息
     * @param data 当次计步信息
     * @return 是否更新成功
     */
    @Override
    public boolean update(final UploadDataEntity data) {
        final UploadDataEntityDao dao = getDao();
        dao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                if (null == data) {
                    return;
                }
                long   insertUsersID = dao.insertOrReplace(data);

            }
        });
        return true;
    }
    //仅保存前十条数据
    public List<UploadDataEntity> queryTenClassUploadData(){
        List<UploadDataEntity> uploadDataEntityList = getDao().queryBuilder().orderAsc(UploadDataEntityDao.Properties.Time).list();
        if(uploadDataEntityList != null){
            if(uploadDataEntityList.size()<10){
                return uploadDataEntityList;
            }else {
                return uploadDataEntityList.subList(0,10);
            }
        }
        return null;
    }
    public   List<UploadDataEntity> queryAllClassUploadData(){
        List<UploadDataEntity> uploadDataEntityList= getDao().loadAll();
        return uploadDataEntityList!= null &&uploadDataEntityList.size()>0 ? uploadDataEntityList: null;
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
    public List<UploadDataEntity> selectAll() {
        return getDao().loadAll();
    }

    @Override
    public List<UploadDataEntity> selectWhere(UploadDataEntity data) {
        return null;
    }

    @Override
    public UploadDataEntity seelctWhrer(String name) {
        return null;
    }

    @Override
    public List<UploadDataEntity> selectWhrer(long id) {
        return null;
    }
}
