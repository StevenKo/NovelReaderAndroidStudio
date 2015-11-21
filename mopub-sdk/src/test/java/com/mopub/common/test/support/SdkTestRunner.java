package com.mopub.common.test.support;

import com.mopub.common.CacheService;
import com.mopub.common.ClientMetadata;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubHttpUrlConnection;
import com.mopub.common.event.EventDispatcher;
import com.mopub.common.event.MoPubEvents;
import com.mopub.common.factories.MethodBuilderFactory;
import com.mopub.common.util.AsyncTasks;
import com.mopub.common.util.DateAndTime;
import com.mopub.common.util.test.support.ShadowAsyncTasks;
import com.mopub.common.util.test.support.ShadowMoPubHttpUrlConnection;
import com.mopub.common.util.test.support.TestDateAndTime;
import com.mopub.common.util.test.support.TestMethodBuilderFactory;
import com.mopub.mobileads.factories.AdViewControllerFactory;
import com.mopub.mobileads.factories.CustomEventBannerAdapterFactory;
import com.mopub.mobileads.factories.CustomEventBannerFactory;
import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;
import com.mopub.mobileads.factories.CustomEventInterstitialFactory;
import com.mopub.mobileads.factories.HtmlBannerWebViewFactory;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;
import com.mopub.mobileads.factories.MoPubViewFactory;
import com.mopub.mobileads.factories.MraidControllerFactory;
import com.mopub.mobileads.factories.VastManagerFactory;
import com.mopub.mobileads.test.support.TestAdViewControllerFactory;
import com.mopub.mobileads.test.support.TestCustomEventBannerAdapterFactory;
import com.mopub.mobileads.test.support.TestCustomEventBannerFactory;
import com.mopub.mobileads.test.support.TestCustomEventInterstitialAdapterFactory;
import com.mopub.mobileads.test.support.TestCustomEventInterstitialFactory;
import com.mopub.mobileads.test.support.TestHtmlBannerWebViewFactory;
import com.mopub.mobileads.test.support.TestHtmlInterstitialWebViewFactory;
import com.mopub.mobileads.test.support.TestMoPubViewFactory;
import com.mopub.mobileads.test.support.TestMraidControllerFactory;
import com.mopub.mobileads.test.support.TestVastManagerFactory;
import com.mopub.nativeads.factories.CustomEventNativeFactory;
import com.mopub.nativeads.test.support.TestCustomEventNativeFactory;

import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.util.concurrent.RoboExecutorService;

import static com.mopub.common.MoPub.LocationAwareness;
import static org.mockito.Mockito.mock;

public class SdkTestRunner extends RobolectricGradleTestRunner {

    public SdkTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public InstrumentationConfiguration createClassLoaderConfig() {
        InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
        builder.addInstrumentedClass(AsyncTasks.class.getName());
        builder.addInstrumentedClass(MoPubHttpUrlConnection.class.getName());
        // To mitigate: https://github.com/robolectric/robolectric/issues/2129
        builder.addInstrumentedPackage("org.xyz.testMp");
        return builder.build();
    }

    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return TestLifeCycleWithInjection.class;
    }

    public static class TestLifeCycleWithInjection extends DefaultTestLifecycle {
        @Override
        public void prepareTest(Object test) {
            ClientMetadata.clearForTesting();

            DateAndTime.setInstance(new TestDateAndTime());
            CustomEventBannerFactory.setInstance(new TestCustomEventBannerFactory());
            CustomEventInterstitialFactory.setInstance(new TestCustomEventInterstitialFactory());
            CustomEventBannerAdapterFactory.setInstance(new TestCustomEventBannerAdapterFactory());
            MoPubViewFactory.setInstance(new TestMoPubViewFactory());
            CustomEventInterstitialAdapterFactory.setInstance(new TestCustomEventInterstitialAdapterFactory());
            HtmlBannerWebViewFactory.setInstance(new TestHtmlBannerWebViewFactory());
            HtmlInterstitialWebViewFactory.setInstance(new TestHtmlInterstitialWebViewFactory());
            AdViewControllerFactory.setInstance(new TestAdViewControllerFactory());
            VastManagerFactory.setInstance(new TestVastManagerFactory());
            MethodBuilderFactory.setInstance(new TestMethodBuilderFactory());
            CustomEventNativeFactory.setInstance(new TestCustomEventNativeFactory());
            MraidControllerFactory.setInstance(new TestMraidControllerFactory());

            ShadowAsyncTasks.reset();
            ShadowMoPubHttpUrlConnection.reset();
            MoPubEvents.setEventDispatcher(mock(EventDispatcher.class));
            MoPub.setLocationAwareness(LocationAwareness.NORMAL);
            MoPub.setLocationPrecision(6);

            MockitoAnnotations.initMocks(test);

            AsyncTasks.setExecutor(new RoboExecutorService());
            CacheService.clearAndNullCaches();
        }
    }
}
