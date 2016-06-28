package com.google.vr.vrcore.settings.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;

import java.util.ArrayList;
import java.util.List;

public class FilteredPackageManager extends PackageManager {

    private final Activity activity;
    private final PackageManager proxy;

    public FilteredPackageManager(Activity a, PackageManager pm) {
        this.activity = a;
        this.proxy = pm;
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        return this.proxy.getPackageInfo(packageName, flags);
    }

    @Override
    public String[] currentToCanonicalPackageNames(String[] names) {
        return this.proxy.currentToCanonicalPackageNames(names);
    }

    @Override
    public String[] canonicalToCurrentPackageNames(String[] names) {
        return this.proxy.canonicalToCurrentPackageNames(names);
    }

    @Override
    public Intent getLaunchIntentForPackage(String packageName) {
        return this.proxy.getLaunchIntentForPackage(packageName);
    }

    @Override
    public Intent getLeanbackLaunchIntentForPackage(String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return this.proxy.getLeanbackLaunchIntentForPackage(packageName);
        }
        return null;
    }

    @Override
    public int[] getPackageGids(String packageName) throws NameNotFoundException {
        return this.proxy.getPackageGids(packageName);
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        return this.proxy.getPermissionInfo(name, flags);
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        return this.proxy.queryPermissionsByGroup(group, flags);
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        return this.proxy.getPermissionGroupInfo(name, flags);
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return this.proxy.getAllPermissionGroups(flags);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        return this.proxy.getApplicationInfo(packageName, flags);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
        return this.proxy.getActivityInfo(component, flags);
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws NameNotFoundException {
        return this.proxy.getReceiverInfo(component, flags);
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags) throws NameNotFoundException {
        return this.proxy.getServiceInfo(component, flags);
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {
        return this.proxy.getProviderInfo(component, flags);
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        return this.proxy.getInstalledPackages(flags);
    }

    @Override
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        return this.proxy.getPackagesHoldingPermissions(permissions, flags);
    }

    @Override
    public int checkPermission(String permName, String pkgName) {
        return this.proxy.checkPermission(permName, pkgName);
    }

    @Override
    public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.proxy.isPermissionRevokedByPolicy(permName, pkgName);
    }

    @Override
    public boolean addPermission(PermissionInfo info) {
        return this.proxy.addPermission(info);
    }

    @Override
    public boolean addPermissionAsync(PermissionInfo info) {
        return this.proxy.addPermissionAsync(info);
    }

    @Override
    public void removePermission(String name) {
        this.proxy.removePermission(name);
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) {
        return this.proxy.checkSignatures(pkg1, pkg2);
    }

    @Override
    public int checkSignatures(int uid1, int uid2) {
        return this.proxy.checkSignatures(uid1, uid2);
    }

    @Override
    public String[] getPackagesForUid(int uid) {
        return this.proxy.getPackagesForUid(uid);
    }

    @Override
    public String getNameForUid(int uid) {
        return this.proxy.getNameForUid(uid);
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return this.proxy.getInstalledApplications(flags);
    }

    @Override
    public String[] getSystemSharedLibraryNames() {
        return this.proxy.getSystemSharedLibraryNames();
    }

    @Override
    public FeatureInfo[] getSystemAvailableFeatures() {
        return this.proxy.getSystemAvailableFeatures();
    }

    @Override
    public boolean hasSystemFeature(String name) {
        return this.proxy.hasSystemFeature(name);
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        return this.proxy.resolveActivity(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        List<ResolveInfo> resolveInfoList = new ArrayList<>();
        for (ResolveInfo ri : this.proxy.queryIntentActivities(intent, flags)) {
            if (!ri.activityInfo.name.equals(this.activity.getClass().getName())) {
                resolveInfoList.add(ri);
            }
        }
        return resolveInfoList;
    }

    @Override
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        return this.proxy.queryIntentActivityOptions(caller, specifics, intent, flags);
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return this.proxy.queryBroadcastReceivers(intent, flags);
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
        return this.proxy.resolveService(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return this.proxy.queryIntentServices(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return this.proxy.queryIntentContentProviders(intent, flags);
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) {
        return this.proxy.resolveContentProvider(name, flags);
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return this.proxy.queryContentProviders(processName, uid, flags);
    }

    @Override
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        return this.proxy.getInstrumentationInfo(className, flags);
    }

    @Override
    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return this.proxy.queryInstrumentation(targetPackage, flags);
    }

    @Override
    public Drawable getDrawable(String packageName, int resId, ApplicationInfo appInfo) {
        return this.proxy.getDrawable(packageName, resId, appInfo);
    }

    @Override
    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        return this.proxy.getActivityIcon(activityName);
    }

    @Override
    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        return this.proxy.getActivityIcon(intent);
    }

    @Override
    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return this.proxy.getActivityBanner(activityName);
        }
        return null;
    }

    @Override
    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return this.proxy.getActivityBanner(intent);
        }
        return null;
    }

    @Override
    public Drawable getDefaultActivityIcon() {
        return this.proxy.getDefaultActivityIcon();
    }

    @Override
    public Drawable getApplicationIcon(ApplicationInfo info) {
        return this.proxy.getApplicationIcon(info);
    }

    @Override
    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        return this.proxy.getApplicationIcon(packageName);
    }

    @Override
    public Drawable getApplicationBanner(ApplicationInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return this.proxy.getApplicationBanner(info);
        }
        return null;
    }

    @Override
    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return this.proxy.getApplicationBanner(packageName);
        }
        return null;
    }

    @Override
    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
        return this.proxy.getActivityLogo(activityName);
    }

    @Override
    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        return this.proxy.getActivityLogo(intent);
    }

    @Override
    public Drawable getApplicationLogo(ApplicationInfo info) {
        return this.proxy.getApplicationLogo(info);
    }

    @Override
    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
        return this.proxy.getApplicationLogo(packageName);
    }

    @Override
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return this.proxy.getUserBadgedIcon(icon, user);
        }
        return null;
    }

    @Override
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return this.proxy.getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
        }
        return null;
    }

    @Override
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return this.proxy.getUserBadgedLabel(label, user);
        }
        return null;
    }

    @Override
    public CharSequence getText(String packageName, int resId, ApplicationInfo appInfo) {
        return this.proxy.getText(packageName, resId, appInfo);
    }

    @Override
    public XmlResourceParser getXml(String packageName, int resId, ApplicationInfo appInfo) {
        return this.proxy.getXml(packageName, resId, appInfo);
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return this.proxy.getApplicationLabel(info);
    }

    @Override
    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        return this.proxy.getResourcesForActivity(activityName);
    }

    @Override
    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
        return this.proxy.getResourcesForApplication(app);
    }

    @Override
    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        return this.proxy.getResourcesForApplication(appPackageName);
    }

    @Override
    public void verifyPendingInstall(int id, int verificationCode) {
        this.proxy.verifyPendingInstall(id, verificationCode);
    }

    @Override
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        this.proxy.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
    }

    @Override
    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        this.proxy.setInstallerPackageName(targetPackage, installerPackageName);
    }

    @Override
    public String getInstallerPackageName(String packageName) {
        return this.proxy.getInstallerPackageName(packageName);
    }

    @Deprecated
    @Override
    public void addPackageToPreferred(String packageName) {
        //this.proxy.addPackageToPreferred(packageName);
    }

    @Deprecated
    @Override
    public void removePackageFromPreferred(String packageName) {
        //this.proxy.removePackageFromPreferred(packageName);
    }

    @Override
    public List<PackageInfo> getPreferredPackages(int flags) {
        return this.proxy.getPreferredPackages(flags);
    }

    @Deprecated
    @Override
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        //this.proxy.addPreferredActivity(filter, match, set, activity);
    }

    @Override
    public void clearPackagePreferredActivities(String packageName) {
        this.proxy.clearPackagePreferredActivities(packageName);
    }

    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        return this.proxy.getPreferredActivities(outFilters, outActivities, packageName);
    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        this.proxy.setComponentEnabledSetting(componentName, newState, flags);
    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
        return this.proxy.getComponentEnabledSetting(componentName);
    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        this.proxy.setApplicationEnabledSetting(packageName, newState, flags);
    }

    @Override
    public int getApplicationEnabledSetting(String packageName) {
        return this.proxy.getApplicationEnabledSetting(packageName);
    }

    @Override
    public boolean isSafeMode() {
        return this.proxy.isSafeMode();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public PackageInstaller getPackageInstaller() {
        return this.proxy.getPackageInstaller();
    }
}
