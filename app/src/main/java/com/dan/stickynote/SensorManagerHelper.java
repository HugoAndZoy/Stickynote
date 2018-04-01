package com.dan.stickynote;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorManagerHelper implements SensorEventListener {

    // 速度阈值，当摇晃速度达到这值后产生作用   The speed threshold, when the shaking speed reaches this value.
    private static final int SPEED_SHRESHOLD = 4500;
    // 两次检测的时间间隔   The time interval between the two tests.
    private static final int UPTATE_INTERVAL_TIME = 50;
    // 传感器管理器    Sensor manager
    private SensorManager sensorManager;
    // 传感器   Sensor
    private Sensor sensor;
    // 重力感应监听器    Gravity sensor
    private OnShakeListener onShakeListener;
    // 上下文对象     context
    private Context context;
    // 手机上一个位置时重力感应坐标   A position on the cell phone, the gravity sensor coordinates
    private float lastX;
    private float lastY;
    private float lastZ;
    // 上次检测时间    Last detection time
    private long lastUpdateTime;
    // 构造器   The constructor
    public SensorManagerHelper(Context context) {
        // 获得监听对象   Get listeners
        this.context = context;
        start();
    }

    // 开始
    public void start() {
        // 获得传感器管理器   Get the sensor manager.
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            // 获得重力传感器    Get the gravity sensor.
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        // 注册  registered
        if (sensor != null) {
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    // 停止检测    Stop testing
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    // 摇晃监听接口    Shaking listening interface
    public interface OnShakeListener {
        public void onShake();
    }

    // 设置重力感应监听器  Set up the gravity sensor monitor.
    public void setOnShakeListener(OnShakeListener listener) {
        onShakeListener = listener;
    }

    /*
     * (non-Javadoc)
     * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
     * .Sensor, int)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    /*
     * 重力感应器感应获得变化数据
     * android.hardware.SensorEventListener#onSensorChanged(android.hardware
     * .SensorEvent)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // 现在检测时间   Detection time
        long currentUpdateTime = System.currentTimeMillis();
        // 两次检测的时间间隔  The time interval between the two tests.
        long timeInterval = currentUpdateTime - lastUpdateTime;
        // 判断是否达到了检测时间间隔  Determine whether the detection time interval is reached.
        if (timeInterval < UPTATE_INTERVAL_TIME) return;
        // 现在的时间变成last时间   turn to last time
        lastUpdateTime = currentUpdateTime;
        // 获得x,y,z坐标   gain the x,y,z position
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        // 获得x,y,z的变化值   get the change in x,y, and z
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;
        // 将现在的坐标变成last坐标    turn the current coordinate into the last coordinate
        lastX = x;
        lastY = y;
        lastZ = z;
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / timeInterval * 10000;
        // 达到速度阀值，发出提示     Reach the speed threshold, issue a prompt
        if (speed >= SPEED_SHRESHOLD)
            onShakeListener.onShake();
    }
}