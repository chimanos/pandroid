package com.leroymerlin.pandroid;

import android.app.Application;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.leroymerlin.pandroid.app.PandroidConfig;
import com.leroymerlin.pandroid.app.PandroidMapper;
import com.leroymerlin.pandroid.app.delegate.PandroidDelegate;
import com.leroymerlin.pandroid.app.delegate.PandroidDelegateProvider;
import com.leroymerlin.pandroid.app.delegate.impl.AutoBinderLifecycleDelegate;
import com.leroymerlin.pandroid.app.delegate.impl.ButterKnifeLifecycleDelegate;
import com.leroymerlin.pandroid.app.delegate.impl.DaggerLifecycleDelegate;
import com.leroymerlin.pandroid.app.delegate.impl.EventBusLifecycleDelegate;
import com.leroymerlin.pandroid.app.delegate.impl.IcepickLifecycleDelegate;
import com.leroymerlin.pandroid.app.delegate.rx.RxLifecycleDelegate;
import com.leroymerlin.pandroid.dagger.BaseComponent;
import com.leroymerlin.pandroid.dagger.DaggerPandroidComponent;
import com.leroymerlin.pandroid.dagger.PandroidModule;
import com.leroymerlin.pandroid.event.EventBusManager;
import com.leroymerlin.pandroid.log.LogWrapper;
import com.leroymerlin.pandroid.log.LogcatLogger;
import com.leroymerlin.pandroid.log.PandroidLogger;

import javax.inject.Inject;


/**
 * Created by florian on 04/12/15.
 */
public class PandroidApplication extends Application implements PandroidDelegateProvider {


    private static final String TAG = "PandroidApplication";

    protected LogWrapper logWrapper;

    protected BaseComponent mBaseComponent;

    @Inject
    EventBusManager eventBusManager;


    @Override
    public void onCreate() {
        super.onCreate();
        initializeBuildConfig();
        initializeLogger();
        inject(this);
    }

    //tag::Logger[]
    protected void initializeLogger() {
        logWrapper = PandroidLogger.getInstance();
        logWrapper.addLogger(LogcatLogger.getInstance());
    }
    //end::Logger[]

    public void inject(Object obj) {
        if (obj != null) {
            BaseComponent baseComponent = getBaseComponent();
            PandroidMapper.getInstance().injectToTarget(baseComponent, obj);
        }
    }

    @VisibleForTesting
    public void overrideBaseComponent(BaseComponent mBaseComponent) {
        this.mBaseComponent = mBaseComponent;
    }

    public BaseComponent getBaseComponent() {
        if (mBaseComponent == null) {
            mBaseComponent = createBaseComponent();
        }
        return mBaseComponent;
    }


    public static PandroidApplication get(Context context) {
        return (PandroidApplication) context.getApplicationContext();
    }

    public void initializeBuildConfig() {
        PandroidMapper.getInstance().setupConfig();
    }


    /**
     * Initialize app base component. Override this methode to give your own component and provide
     * new manager to PandroidFragment/PandroidActivity
     *
     * @return BaseComponent that will be used to inject manager in Pandroid Classes
     */
    protected BaseComponent createBaseComponent() {
        return DaggerPandroidComponent.builder()
                .pandroidModule(new PandroidModule(this))
                .build();
    }

    //tag::PandroidBaseLifecycleDelegate[]

    /**
     * initialize LifecycleDelegate that will listen the activity life cycle
     * Override this method to disable or add more delegate
     *
     * @return PandroidDelegate that will be used in pandroid activity / pandroid fragment
     */
    protected PandroidDelegate createBasePandroidDelegate() {
        PandroidDelegate pandroidDelegate = new PandroidDelegate();
        pandroidDelegate.addLifecycleDelegate(new DaggerLifecycleDelegate());
        pandroidDelegate.addLifecycleDelegate(new EventBusLifecycleDelegate(eventBusManager));
        pandroidDelegate.addLifecycleDelegate(new AutoBinderLifecycleDelegate());
        if (PandroidConfig.isLibraryEnable("butterknife")) {
            pandroidDelegate.addLifecycleDelegate(new ButterKnifeLifecycleDelegate());
        } else {
            logWrapper.v(TAG, "ButterKnife is disabled, add the library in Pandroid extension to use it");
        }
        if (PandroidConfig.isLibraryEnable("icepick")) {
            pandroidDelegate.addLifecycleDelegate(new IcepickLifecycleDelegate());
        } else {
            logWrapper.v(TAG, "Icepick is disabled, add the library in Pandroid extension to use it");
        }
        if (PandroidConfig.isLibraryEnable("rxandroid")) {
            pandroidDelegate.addLifecycleDelegate(new RxLifecycleDelegate());
        } else {
            logWrapper.v(TAG, "RxAndroid is disabled, add the library in Pandroid extension to use it");
        }
        return pandroidDelegate;
    }

    @Override
    public PandroidDelegate getPandroidDelegate() {
        return createBasePandroidDelegate();
    }
    //tag::PandroidBaseLifecycleDelegate[]

}
