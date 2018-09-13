package com.yt.sportservice.manager.media;


import com.yt.sportservice.util.LogUtils;

/**
 * @author mare
 * @Description:TODO 媒体文件播放失败错误解析
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/11/24
 * @time 12:42
 */
public class MediaErrorParser {

    public enum MeidaType {
        MUSIC, IMG, VIDEO
    }

    public static void parse(int ErrorCode, int extra) {
        LogUtils.e("playSound OnError - Error code: " + ErrorCode +
                " extra: " + extra);
        switch (ErrorCode) {
            case -1004:
                LogUtils.d("MEDIA_ERROR_IO");
                break;
            case -1007:
                LogUtils.d("MEDIA_ERROR_MALFORMED");
                break;
            case 200:
                LogUtils.d("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                break;
            case 100:
                LogUtils.d("MEDIA_ERROR_SERVER_DIED");
                break;
            case -110:
                LogUtils.d("MEDIA_ERROR_TIMED_OUT");
                break;
            case 1:
                LogUtils.d("MEDIA_ERROR_UNKNOWN");
                break;
            case -1010:
                LogUtils.d("MEDIA_ERROR_UNSUPPORTED");
                break;
        }
        switch (extra) {
            case 800:
                LogUtils.d("MEDIA_INFO_BAD_INTERLEAVING");
                break;
            case 702:
                LogUtils.d("MEDIA_INFO_BUFFERING_END");
                break;
            case 701:
                LogUtils.d("MEDIA_INFO_METADATA_UPDATE");
                break;
            case 802:
                LogUtils.d("MEDIA_INFO_METADATA_UPDATE");
                break;
            case 801:
                LogUtils.d("MEDIA_INFO_NOT_SEEKABLE");
                break;
            case 1:
                LogUtils.d("MEDIA_INFO_UNKNOWN");
                break;
            case 3:
                LogUtils.d("MEDIA_INFO_VIDEO_RENDERING_START");
                break;
            case 700:
                LogUtils.d("MEDIA_INFO_VIDEO_TRACK_LAGGING");
                break;
        }
    }
}
