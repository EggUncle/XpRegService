package com.android.server;


import android.os.Build;
import android.os.IBinder;
import android.os.ICustomService;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by songyucheng on 18-3-28.
 */

public class CustomService extends ICustomService.Stub {
    private final static String SERVICE_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "user.xpservice" : "xpservice";
    private final static String AMS_CLASS = "com.android.server.am.ActivityManagerService";
    private final static String SYM_CLASS = "android.os.ServiceManager";

    private CustomService mCustomService;

    private static ICustomService mClient;

    public static ICustomService getClient() {
        if (mClient == null) {

            Class<?> ServiceManager = null;
            try {
                ServiceManager = Class.forName("android.os.ServiceManager");
                Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
                mClient = ICustomService.Stub.asInterface((IBinder) getService.invoke(null, SERVICE_NAME));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        return mClient;
    }


    public void register(final ClassLoader classLoader) {
        Class<?> ActivityManagerService = XposedHelpers.findClass(AMS_CLASS, classLoader);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            XposedBridge.hookAllConstructors(ActivityManagerService, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    addService(classLoader);
                }
            });
        } else {
            XposedBridge.hookAllMethods(ActivityManagerService, "main", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    addService(classLoader);
                }
            });
        }

        XposedBridge.hookAllMethods(ActivityManagerService, "systemReady", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mCustomService.systemReady();
            }
        });
    }

    private void addService(ClassLoader classLoader) {
        mCustomService = new CustomService();

        Class<?> ServiceManager = XposedHelpers.findClass(SYM_CLASS, classLoader);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            XposedHelpers.callStaticMethod(
                    ServiceManager,
                    "addService",
                    SERVICE_NAME,
                    mCustomService,
                    true
            );
        } else {
            XposedHelpers.callStaticMethod(
                    ServiceManager,
                    "addService",
                    SERVICE_NAME,
                    mCustomService
            );
        }
    }

    private void systemReady() {
        // Make initialization here
    }

    @Override
    public String getDataFromService() throws RemoteException {
        return "xp_reg_sys_server";
    }
}
