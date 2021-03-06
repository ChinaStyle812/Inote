package com.lf.inote.net;


public interface NetWorkCallback {
	/**
     * 成功才调用该方法 返回数据处理
     * @param result  返回值
     */
    void onSuccess(String result);
    
    /**
     * 客户端请求失败
     */
    void onError();
}
