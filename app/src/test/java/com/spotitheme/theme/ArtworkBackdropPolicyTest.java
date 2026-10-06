package com.spotitheme.theme;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.spotitheme.hooks.ArtworkBackdropPolicy;
import org.junit.Test;

public final class ArtworkBackdropPolicyTest {
    @Test public void hidesNativeBlurByDefaultWhileThemeIsEnabled() {
        assertTrue(ArtworkBackdropPolicy.shouldHide(true, false));
    }

    @Test public void respectsExplicitShowSetting() {
        assertFalse(ArtworkBackdropPolicy.shouldHide(true, true));
    }

    @Test public void restoresNativeBlurWhenThemeIsDisabled() {
        assertFalse(ArtworkBackdropPolicy.shouldHide(false, false));
    }
}
