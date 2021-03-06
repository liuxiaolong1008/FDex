package com.example.fdex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import android.os.Environment;
import android.text.TextUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class DexHook implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		// TODO Auto-generated method stub
		XSharedPreferences preferences = new XSharedPreferences("com.example.fdex");
		String packageName = preferences.getString("packageName", null);
		String appName = preferences.getString("appName", null);
		if(TextUtils.isEmpty(packageName))
			return;
		if(packageName.equals(lpparam.packageName)){
			System.out.println("app " + appName + " launch");
			Class<?> dexClazz = Class.forName("com.android.dex.Dex");
			Class<?> clazz = Class.forName("java.lang.Class");
			final Method getDexMd = clazz.getDeclaredMethod("getDex");
			final Method getBytesMd = dexClazz.getDeclaredMethod("getBytes");
			XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					// TODO Auto-generated method stub
					if(param.hasThrowable())
						return;
					Class<?> clazz = (Class<?>) param.getResult();
					if(clazz != null){
						System.out.println(clazz.getName());
						Object dex = getDexMd.invoke(clazz);
						byte[] dexBytes = (byte[]) getBytesMd.invoke(dex);
						if(dexBytes != null){
							File dexFile = new File("/data/data/"+lpparam.packageName, "dump_dex_"+dexBytes.length+".dex");
							if(dexFile.exists()){
								return;
							}else{
								FileOutputStream fos = new FileOutputStream(dexFile);
								fos.write(dexBytes);
								fos.close();
							}
						}
					}
				}
			});
		}
	}

}
