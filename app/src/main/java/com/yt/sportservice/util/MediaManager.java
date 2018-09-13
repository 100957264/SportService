package com.yt.sportservice.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;

import com.yt.sportservice.KApplication;
import com.yt.sportservice.manager.media.MediaErrorParser;

import java.io.IOException;

/**
 * 作者：Rance on 2016/12/15 15:11
 * 邮箱：rance935@163.com
 */
public class MediaManager {

    private static MediaPlayer mMediaPlayer;
    private static boolean isPause;

    /**
     * 播放音乐
     *
     * @param filePath
     * @param onCompletionListener
     */
    public static void playSound(final String filePath, final OnCompletionListener onCompletionListener
            , MediaPlayer.OnBufferingUpdateListener updateListener) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            if (null != updateListener) {
                mMediaPlayer.setOnBufferingUpdateListener(updateListener);
            }
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    LogUtils.e("onError ");
                    MediaErrorParser.parse(what, extra);
                    if (null != onCompletionListener) {
                        onCompletionListener.onCompletion(mp);
                    }
                    stopFindDevice();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
            LogUtils.e("playSound playSound :reset ");
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (Exception e) {
            LogUtils.e("MediaManager playSound Exception" + e);
        }
    }

    public static void getAudioDurationByPath(String path,MediaPlayer.OnPreparedListener listener)  {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }else{
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setDataSource(path);
//            mMediaPlayer.prepare();
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("getAudioDurationByPath error " +e);
        }
    }

    /**
     * 播放音乐
     *
     * @param res
     */
    public static void findDevice(int res, boolean loop) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {

                public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }
        try {
            Context ctx = KApplication.sContext;
            String uriStr = "android.resource://" + ctx.getPackageName() + "/" + res;
            Uri dataUri = Uri.parse(uriStr);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(loop);
            mMediaPlayer.setDataSource(ctx, dataUri);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            if (!loop) {//不是循环时候 要回收player
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopFindDevice();
                    }
                });
            }
        } catch (Exception e) {
            LogUtils.e("查找设备播放音效失败 : " + e);
        }
    }
    /**
     * 播放在线音乐
     */
    public static void playOnlineMp3(String url,boolean loop){
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {

                public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(loop);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            if (!loop) {//不是循环时候 要回收player
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopFindDevice();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void adjustVolume(int res, boolean loop,int type) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {

                public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mMediaPlayer.reset();
        }
        try {
            Context ctx = KApplication.sContext;
            String uriStr = "android.resource://" + ctx.getPackageName() + "/" + res;
            Uri dataUri = Uri.parse(uriStr);
            mMediaPlayer.setAudioStreamType(type);
            mMediaPlayer.setLooping(loop);
            mMediaPlayer.setDataSource(ctx, dataUri);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            if (!loop) {//不是循环时候 要回收player
                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopFindDevice();
                    }
                });
            }
        } catch (Exception e) {
            LogUtils.e("查找设备播放音效失败 : " + e);
        }
    }

    /**
     * 暂停播放
     */
    public static void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) { //正在播放的时候
            mMediaPlayer.pause();
            isPause = true;
        }
    }

    /**
     * 当前是isPause状态
     */
    public static void resume() {
        if (mMediaPlayer != null && isPause) {
            mMediaPlayer.start();
            isPause = false;
        }
    }

    public static void stopFindDevice() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();//在停止
            mMediaPlayer.setOnBufferingUpdateListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.reset();//消除source流的引用(防止内存泄漏)
            mMediaPlayer.release();//最后释放
            mMediaPlayer = null;
        }
    }

}
