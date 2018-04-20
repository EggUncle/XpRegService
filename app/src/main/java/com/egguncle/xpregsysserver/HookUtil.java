package com.egguncle.xpregsysserver;

import com.android.server.CustomService;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by songyucheng on 18-3-28.
 */

public class HookUtil implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private CustomService customService;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            customService.register(lpparam.classLoader);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        customService = new CustomService();
    }
}
