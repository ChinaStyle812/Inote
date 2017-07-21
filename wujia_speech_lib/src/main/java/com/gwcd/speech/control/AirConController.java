package com.gwcd.speech.control;

import android.os.Bundle;

/**
 * Created by sy on 2017/3/22.<br>
 * Function: <br>
 * Creator: sy<br>
 * Create time: 2017/3/22 15:53<br>
 * Revise Record:<br>
 * 2017/3/22: 创建并完成初始实现<br>
 */

public interface AirConController extends WuSpeechController {

    /**
     * 设置开关
     * @param data
     */
    void setPower(Bundle data);

    /**
     * 设置模式
     * @param data
     */
    void setMode(Bundle data);

    /**
     * 设置温度
     * @param data
     */
    void setTemp(Bundle data);

    /**
     * 设置风力
     * @param data
     */
    void setWind(Bundle data);
}
