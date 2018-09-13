package com.yt.sportservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;


import com.yt.sportservice.KApplication;
import com.yt.sportservice.entity.CdmaID;
import com.yt.sportservice.entity.GSMNeighboringCellInfo;
import com.yt.sportservice.entity.SCell;
import com.yt.sportservice.util.LogUtils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author mare
 * @Description:TODO
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2018/1/30
 * @time 20:40
 */
public class FunctionLocService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void pull() {
        Context context = KApplication.sContext;
        context.startService(new Intent(context, FunctionLocService.class));
    }

    public static void stop() {
        Context ctx = KApplication.sContext;
        ctx.stopService(new Intent(ctx, FunctionLocService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e("FunctionLocService is running ....");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startParseBaseStation();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e("FunctionLocService is destroyed ....");
    }

    private void startParseBaseStation() {
        TelephonyManager tm = (TelephonyManager) KApplication.sContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (null == tm) {
            stopSelf();//自杀
            return;
        }
        // 返回值MCC + MNC
        String operator = tm.getNetworkOperator();
        if (TextUtils.isEmpty(operator)) {
            LogUtils.e("mare operator null");
            return;
        }
        LogUtils.e("mare operator " + operator);
        if (operator == null || operator.length() < 5) {
            LogUtils.e("mare " + "获取基站信息有问题,可能是手机没插sim卡");
            return;
        }
        String mcc = operator.substring(0, 3);
        String mnc = operator.substring(3);
        LogUtils.e("mare mcc - mnc : " + mcc + " - " + mnc);

        CellLocation cellLocation = tm.getCellLocation();
        if (cellLocation == null) {
            LogUtils.e("mare " + "手机没插sim卡吧");
            return;
        }
        // 中国移动和中国联通获取LAC、CID的方式
        boolean isGsm = tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM;
        boolean isCdma = tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA;
        int cid = 0, lac = 0;
        int nid = 0, bid = 0, sid = 0;

        SCell sCell = null;
        CdmaID cdmaID = null;
        if (isCdma) {
            // 中国电信获取LAC、CID的方式
            LogUtils.e("mare " + "现在连接的是cdma基站");
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
            if (null == cdmaCellLocation) {
                LogUtils.e("mare 没连上电信基站.....");
                return;
            }
            cid = cdmaCellLocation.getBaseStationId();
            lac = cdmaCellLocation.getNetworkId();
            nid = cdmaCellLocation.getNetworkId(); //获取cdma网络编号NID return cdma network identification number, -1 if unknown
            bid = cdmaCellLocation.getBaseStationId(); //获取cdma基站识别标号 BID
            sid = cdmaCellLocation.getSystemId(); //用谷歌API的话cdma网络的mnc要用这个getSystemId()取得→SID
            LogUtils.e("mare " + " cdma基站 = " + "\t LAC位置区域码 = " + lac + "\t CID基站编号 = " + cid);
        } else if (isGsm) {
            LogUtils.e("mare " + "当前连接的是gsm基站");
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
            if (null == gsmCellLocation) {
                LogUtils.e("mare 没连上联通移动基站.....");
                return;
            }
            lac = gsmCellLocation.getLac(); //连接的gsm网络编号
            cid = gsmCellLocation.getCid(); //连接的gsm基站识别标号
            int psc = gsmCellLocation.getPsc();
            sCell = new SCell(mcc, mnc, lac, cid);
            LogUtils.e("mare gsm基站 " + gsmCellLocation.toString());
            sCell.setGSMDelay(psc);//暂时用psc当做时延
            LogUtils.e("mare " + " gsm基站 " + "\t LAC位置区域码 = " + lac + "\t CID基站编号 = " + cid);
        }

        List<CellInfo> infoLists = tm.getAllCellInfo();

        boolean isCellInfoGsm = false;
        List<GSMNeighboringCellInfo> gsmNeighboringCellInfos = new ArrayList<>();
        GSMNeighboringCellInfo gsmNeighboringCellInfo;
        int neighboringLac;
        int neighboringCid;
        int neighboringBsss;
        int rsp = 0;
        if (null == infoLists || (infoLists!= null && infoLists.size() == 0)) {
            infoLists = new ArrayList<>();
            neighboringBsss = -113 + 2 * rsp; // 获取邻区基站信号强度
            neighboringLac = lac;
            neighboringCid = cid;
            LogUtils.e("mare 临近基站个数为0，使用自身基站..lac=" + lac + ",cid=" + cid);
            if(neighboringCid > 0 && neighboringCid != 65535 && neighboringLac != 65535) {
                gsmNeighboringCellInfo = new GSMNeighboringCellInfo(neighboringLac, neighboringCid, neighboringBsss);
                gsmNeighboringCellInfos.add(gsmNeighboringCellInfo);
            }
        } else {
            //TODO 筛选7个 按强度排序
        }
        for (CellInfo info : infoLists) {
            isCellInfoGsm = false;
            rsp = 0;
            if (info instanceof CellInfoGsm) {//通用的移动联通电信2G的基站数据
                isCellInfoGsm = true;
                CellInfoGsm cellInfoGsm = (CellInfoGsm) info;
                LogUtils.e("mare " + "当前是gsm基站 cellInfoGsm ");
                CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
                CellSignalStrengthGsm cellrsp = cellInfoGsm.getCellSignalStrength();
                rsp = cellrsp.getDbm();
                lac = cellIdentity.getLac();
                cid = cellIdentity.getCid();
                LogUtils.e("mare " + " MCC移动国家代码 = " + mcc + "\t MNC移动网络号码 = "
                        + mnc + "\t LAC位置区域码 = " + lac + "\t CID基站编号 = " + cid + " ,信号强度 " + rsp);
            } else if (info instanceof CellInfoLte) {//4g网络的基站数据
                isCellInfoGsm = true;
                CellInfoLte cellInfoLte = (CellInfoLte) info;
                LogUtils.e("mare " + "当前是gsm基站 cellInfoGsm ");
                CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
                CellSignalStrengthLte cellrsp = cellInfoLte.getCellSignalStrength();
                rsp = cellrsp.getDbm();
                lac = cellIdentity.getTac();
                cid = cellIdentity.getCi();
                LogUtils.e("mare " + " ,MCC移动国家代码 = " + mcc + "\t ,MNC移动网络号码 = "
                        + mnc + "\t ,LAC位置区域码 = " + lac + "\t ,CID基站编号 = " + cid + " ,信号强度 " + rsp);
            } else if (info instanceof CellInfoWcdma) {//联通3G的基站数据
                isCellInfoGsm = true;
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) info;
                LogUtils.e("mare " + "当前是gsm基站 cellInfoGsm ");
                CellIdentityWcdma cellIdentity = cellInfoWcdma.getCellIdentity();
                CellSignalStrengthWcdma cellrsp = cellInfoWcdma.getCellSignalStrength();
                rsp = cellrsp.getDbm();
                lac = cellIdentity.getLac();
                cid = cellIdentity.getCid();
                LogUtils.e("mare " + " MCC移动国家代码 = " + mcc + "\t MNC移动网络号码 = "
                        + mnc + "\t LAC位置区域码 = " + lac + "\t CID基站编号 = " + cid + " ,信号强度 " + rsp);
            } else if (info instanceof CellInfoCdma) {//电信3G的基站数据
                //TODO 电信基站信息暂时过滤
                isCellInfoGsm = false;
                CellInfoCdma cellInfoCdma = (CellInfoCdma) info;
                if (null == info) {
                    LogUtils.e("mare 获取电信基站信息为空.....");
                    return;
                }
                LogUtils.d("mare 电信基站信息： ");
                CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                rsp = cellSignalStrengthCdma.getCdmaDbm();
                bid = cellIdentityCdma.getBasestationId();// 处理 strength和id数据
                sid = cellIdentityCdma.getSystemId();
                nid = cellIdentityCdma.getNetworkId();
                LogUtils.e("mare " + "电信网络编号NID = "
                        + nid + "\t 基站识别标号 BID = " + bid + "\t SID = " + sid + " ,信号强度 " + rsp);
            } else {
                isCellInfoGsm = false;
                LogUtils.e("mare " + "info name " + info.getClass().getSimpleName());
                LogUtils.e("mare " + "现在不知道是什么鬼基站信息");
            }
            if (isCellInfoGsm) {
                neighboringBsss = -113 + 2 * rsp; // 获取邻区基站信号强度
                neighboringLac = lac;
                neighboringCid = cid;
                if(neighboringCid > 0 && neighboringCid != 65535 && neighboringLac != 65535) {
                    gsmNeighboringCellInfo = new GSMNeighboringCellInfo(neighboringLac, neighboringCid, neighboringBsss);
                    gsmNeighboringCellInfos.add(gsmNeighboringCellInfo);
                }
            }
//            LogUtils.e("mare infoLists info " + info.toString());
        }
        LogUtils.e("mare infoLists.size " + infoLists.size());

            List<GSMNeighboringCellInfo> gsmNeighboringResult = new ArrayList<>();//存放排好序的基站信息
            sortStationByLevel(gsmNeighboringCellInfos);
            int maxLen = Math.min(7, gsmNeighboringCellInfos.size());//最多7个基站信息
            for (int i = 0; i < maxLen; i++) {
                gsmNeighboringResult.add(gsmNeighboringCellInfos.get(i));
            }
            int baseStationCount = gsmNeighboringResult.size();
            LogUtils.e("mare gsm附近基站个数： " + baseStationCount);
        //================建议放在service里=============
    }

    /**
     * 信号强度从强到时弱进行排序
     *
     * @param list 存放周围基站信息对象的列表
     */
    private static void sortStationByLevel(List<GSMNeighboringCellInfo> list) {

        Collections.sort(list, new Comparator<GSMNeighboringCellInfo>() {

            @Override
            public int compare(GSMNeighboringCellInfo lhs, GSMNeighboringCellInfo rhs) {
                return rhs.neighboringBiss - lhs.neighboringBiss;
            }
        });
    }
}
