package com.spotitheme.profile;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pinned identities verified against Spotify's APK. No discovery fallback is allowed. */
public final class Profile_9_1_86_2432 {
    public static final String VERSION_NAME = "9.1.86.2432";
    public static final long VERSION_CODE = 146555520L;
    public static final String MATERIAL_COLOR_FIELDS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUV";
    private static final Map<String, MethodTarget> TARGETS;
    private static final Map<String, FieldTarget> FIELDS;
    static {
        Map<String, MethodTarget> targets = new LinkedHashMap<>();
        add(targets, "encore.layout", "Lp/vnv;->a(ILp/b710;Lp/fg10;II)V");
        add(targets, "encore.resolver", "Lp/tnv;->h(ILp/fg10;)Lp/u4v;");
        add(targets, "encore.accessor", "Lp/y0v;->a(Lp/fg10;)Lp/u4v;");
        add(targets, "encore.provider.c", "Lp/b5v;->c(Lp/u4v;Lp/jdg;Lp/fg10;I)V");
        add(targets, "encore.provider.d", "Lp/b5v;->d(Lp/u4v;Lp/jdg;Lp/fg10;I)V");
        add(targets, "material.theme", "Lp/zxd0;->a(Lp/pnf;Lp/zzi0;Lp/vs11;Lp/n4b1;Lp/jdg;Lp/fg10;I)V");
        add(targets, "compose.dispatchDraw", "Lp/v24;->dispatchDraw(Landroid/graphics/Canvas;)V");
        add(targets, "compose.albumGradient", "Lp/c791;->f(Lp/kha0;Lp/fg10;I)V");
        add(targets, "compose.stateFactory", "Lp/i7v0;->A(Ljava/lang/Object;)Lp/fwn0;");
        add(targets, "compose.localRead", "Lp/fg10;->k(Lp/e7u0;)Ljava/lang/Object;");
        add(targets, "compose.stateRead", "Lp/fwn0;->getValue()Ljava/lang/Object;");
        add(targets, "compose.stateWrite", "Lp/fwn0;->setValue(Ljava/lang/Object;)V");
        add(targets, "row.factory", "Lp/oin;->c(Landroid/view/LayoutInflater;)Lp/oin;");
        add(targets, "glyph.colorState", "Lp/bp51;->c(Landroid/content/res/ColorStateList;)V");
        add(targets, "transport.circleState", "Lp/bse;->onStateChange([I)Z");
        add(targets, "lottie.callback", "Lp/d4c0;->a(Lp/vo70;Ljava/lang/Object;Lp/cca0;)V");
        add(targets, "lottie.callbackConstructor", "Lp/cca0;-><init>(I)V");
        add(targets, "lottie.keyPathConstructor", "Lp/vo70;-><init>([Ljava/lang/String;)V");
        add(targets, "saved.materialIcon", "Lcom/google/android/material/button/MaterialButton;->getIcon()Landroid/graphics/drawable/Drawable;");
        add(targets, "mini.backgroundSet", "Lcom/spotify/mainlayout/ui/view/containers/MainLayoutContentContainerView;->setBackgroundColor-8_81llA(J)V");
        add(targets, "mini.backgroundGet", "Lcom/spotify/mainlayout/ui/view/containers/MainLayoutContentContainerView;->getBackgroundColor-0d7_KjU()J");
        add(targets, "mini.spanConstructor", "Lp/bf81;-><init>(Landroid/content/Context;II)V");
        add(targets, "mini.spanDraw", "Lp/bf81;->updateDrawState(Landroid/text/TextPaint;)V");
        add(targets, "lottie.value", "Lp/cca0;->k(FFLjava/lang/Object;Ljava/lang/Object;FFF)Ljava/lang/Object;");
        add(targets, "credits.card", "Lp/e5h1;->b(Lp/nf30;Lp/wrn;Lp/rmm;Ljava/lang/String;Lp/x650;Lp/hvi0;ZLp/fg10;I)V");
        add(targets, "songDna.tintConstructor", "Lp/sv9;-><init>(IJ)V");
        add(targets, "compose.blendMode", "Lp/tfk;->n0(I)Landroid/graphics/BlendMode;");
        add(targets, "songDna.logo", "Lp/d961;->f(Lp/hvi0;Lp/i5n0;Lp/fg10;II)V");
        add(targets, "songDna.contributor", "Lp/gr41;->a(Lp/cx41;Lp/hvi0;Lp/fg10;I)V");
        add(targets, "songDna.card", "Lp/gr41;->e(Lp/gx41;Lp/o610;Lp/fg10;I)V");
        add(targets, "songDna.content", "Lp/rw41;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "compose.image", "Lp/zn71;->d(Lp/x450;Lp/hvi0;Lp/yxs;Lp/llv;Lp/a2v;Lp/wak;Lp/vd3;Landroidx/compose/ui/graphics/ColorFilter;Lp/fg10;II)V");
        add(targets, "compose.text", "Lp/n6h1;->c(Ljava/lang/String;Lp/hvi0;Lp/xs81;JLp/qe81;Lp/o610;IZLp/lg90;ILp/c68;Lp/fg10;III)V");
        add(targets, "compose.colorConstructor", "Lp/glf;-><init>(J)V");
        add(targets, "songDna.gradient", "Lp/iz41;->a(Lp/glf;JJLp/hvi0;ZLp/z45;Lp/jdg;Lp/fg10;II)V");
        add(targets, "player.closePalette", "Lp/isw;->a(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "search.landing", "Lp/t2i1;->d(ILjava/lang/String;Ljava/lang/String;Lp/fg10;Lp/hvi0;)V");
        add(targets, "creation.background", "Lp/y8j;->e(Lp/y8j;Lp/wti0;Lp/o610;Lp/i5n0;Lp/fg10;I)V");
        add(targets, "creation.heading", "Lp/y8j;->d(ILp/fg10;)V");
        add(targets, "library.header", "Lp/gr41;->b(Lp/lvy;Lp/o610;Lp/hvi0;Lp/jdg;Lp/jdg;Lp/fg10;I)V");
        add(targets, "playlist.titleOwner", "Lp/plu0;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "playlist.creatorOwner", "Lp/x3h1;->c(Ljava/lang/String;Lp/m610;ZLp/b7m;Lp/x650;Lp/jdg;Lp/wyl;Lp/o610;Lp/ds3;Lp/fg10;I)V");
        add(targets, "compose.annotatedText", "Lp/n6h1;->d(Lp/u55;Lp/hvi0;Lp/xs81;JLp/qe81;Lp/o610;IZLp/lg90;ILp/c68;Lp/pso0;Lp/fg10;III)V");
        add(targets, "creation.modelConstructor", "Lp/fsl;-><init>(ZLp/esl;)V");
        add(targets, "creation.input", "Lp/y8j;->c(Ljava/lang/String;Lp/tm81;Lp/hvi0;Lp/m610;Lp/fg10;I)V");
        add(targets, "compose.gradientConstructor", "Lp/uf90;-><init>(IJJLjava/util/List;Ljava/util/List;)V");
        add(targets, "creation.fieldContent", "Lp/f6h1;->a(Lp/tm81;Lp/hvi0;Lp/h0v;Ljava/lang/String;Lp/b710;Lp/b710;ZZLp/fp50;Lp/xs81;Lp/yt70;Lp/mt70;Lp/yk81;Lp/knj0;Lp/wri1;Lp/stz0;Lp/i5n0;FLp/inv;Lp/fg10;III)V");
        add(targets, "compose.icon", "Lp/mnf1;->a(Lp/rjv;Lp/fxj;Lp/hvi0;JJZLp/fg10;II)V");
        add(targets, "compose.roundedFill", "Lp/l9c;->l(Lp/hvi0;JLp/qr11;)Lp/hvi0;");
        add(targets, "autoTheme.consumer.jbc0", "Lp/jbc0;->accept(Ljava/lang/Object;)V");
        add(targets, "autoTheme.consumer.q2q0", "Lp/q2q0;->accept(Ljava/lang/Object;)V");
        add(targets, "autoTheme.consumer.yqe1", "Lp/yqe1;->accept(Ljava/lang/Object;)V");
        add(targets, "entity.title", "Lp/gcw;->invoke(Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "albumGradient.emit", "Lp/f080;->emit(Ljava/lang/Object;Lp/nrk;)Ljava/lang/Object;");
        add(targets, "albumGradient.coroutine", "Lp/vv01;->c(Ljava/lang/Object;Lp/nrk;)Ljava/lang/Object;");
        add(targets, "albumGradient.flow", "Lp/vv01;->emit(Ljava/lang/Object;Lp/nrk;)Ljava/lang/Object;");
        add(targets, "compose.linearGradient", "Lp/zgi1;->j(IJJLjava/util/List;Ljava/util/List;)Landroid/graphics/LinearGradient;");
        add(targets, "compose.colorRect", "Landroidx/compose/ui/graphics/drawscope/DrawScope;->h0(Landroidx/compose/ui/graphics/drawscope/DrawScope;JJJFLp/gz61;Landroidx/compose/ui/graphics/ColorFilter;I)V");
        add(targets, "artist.scrim", "Lp/qoi1;->d(Lp/hvi0;FFLp/b710;Lp/fg10;II)V");
        add(targets, "countdown.titleOwner", "Lp/cw5;->E0(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "countdown.model", "Lp/qj91;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lp/p0l;Ljava/lang/Integer;)V");
        add(targets, "home.header", "Lp/m810;-><init>(Ljava/lang/String;Landroid/content/Context;Lp/qk00;Lp/s5v;Lp/jao;Lp/jtz0;Lp/ufa0;Lp/pln0;Landroid/os/Bundle;Lp/g861;Lp/apj0;Lp/be;Lp/jaa0;Lp/gh5;ZLp/gu4;ZLp/n5b1;)V");
        add(targets, "album.header", "Lp/zwl;-><init>(Landroid/content/Context;Lp/l050;Landroid/view/View;Lp/gvl;Landroid/view/View;ZLandroid/view/View;Landroid/view/View;Landroid/view/View;Landroid/view/View;Landroid/view/View;Landroid/view/View;Landroid/view/View;Landroid/view/View;Landroid/view/View;Landroid/view/View;ZZLp/ds1;)V");
        add(targets, "album.header.view", "Lp/zwl;->getView()Landroid/view/View;");
        add(targets, "album.header.update", "Lp/zwl;->e(Ljava/lang/Object;)V");
        add(targets, "artist.header", "Lp/o3q;-><init>(Landroid/content/Context;Lp/l050;Z)V");
        add(targets, "artist.header.view", "Lp/o3q;->getView()Landroid/view/View;");
        add(targets, "artist.header.update", "Lp/o3q;->e(Ljava/lang/Object;)V");
        add(targets, "saved.viewState", "Lcom/spotify/encoreconsumermobile/elements/addtobutton/AddToButtonView;->a(Lp/lj1;)V");
        add(targets, "saved.encoreState", "Lcom/spotify/encoreconsumermobile/elements/addtobutton/EncoreAddToButtonView;->g(Lp/lj1;)V");
        add(targets, "home.filterChip", "Lp/wv30;->d(Lp/c1v;Lp/b9;Lp/i4v;ZLp/hvi0;Lp/knj0;ZLp/i5n0;Lp/b710;Lp/fg10;II)V");
        add(targets, "home.filterChipSimpleStyle", "Lp/j4v;-><init>(I)V");
        add(targets, "home.filterChipStyle", "Lp/k4v;-><init>(ILp/b710;Lp/b710;Lp/b710;I)V");
        add(targets, "artwork.overlayStateChange", "Lp/xxd0;->onStateChange([I)Z");
        add(targets, "drawer.constructor", "Lp/b931;-><init>(Lp/wt00;Lp/l931;Lp/wfn0;Lp/ydo;)V");
        add(targets, "biography.row", "Lp/csp;->invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "biography.following", "Lp/txg;->d1(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "glyph.color", "Lp/bp51;->b(I)V");
        add(targets, "player.overlay", "Lcom/spotify/nowplaying/uiusecases/overlay/OverlayHidingGradientBackgroundView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V");
        add(targets, "player.overlayColor", "Lcom/spotify/nowplaying/uiusecases/overlay/OverlayHidingGradientBackgroundView;->setColor(I)V");
        add(targets, "navigation.gradient", "Lcom/spotify/mainlayout/ui/view/gradient/MainLayoutGradientView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V");
        add(targets, "navigation.bottomColor", "Lcom/spotify/mainlayout/ui/view/gradient/MainLayoutGradientView;->setBottomColor(Ljava/lang/Integer;)V");
        add(targets, "album.header.artworkCallback", "Lp/drl;->t(Ljava/lang/Object;)V");
        add(targets, "entity.titleBinding", "Lp/qfo;->r(Landroid/view/LayoutInflater;Lp/xjw;)Lp/qfo;");
        add(targets, "entity.colorResolver", "Lp/lii1;->w(Landroid/view/View;I)I");
        add(targets, "row.colorStateResolver", "Lp/tfk;->w(Landroid/content/Context;I)Landroid/content/res/ColorStateList;");
        add(targets, "album.composeGradientFactory", "Lp/x85;->F(Ljava/util/List;FFI)Lp/uf90;");
        add(targets, "album.gradient", "Lp/a830;-><init>(Ljava/util/List;)V");
        add(targets, "album.positionedGradient", "Lp/b830;-><init>(FLjava/util/List;)V");
        add(targets, "encore.paletteConstructor", "Lp/u4v;-><init>(Lp/f2v;Lp/hnv;Lp/i7v;Lp/q6v;)V");
        add(targets, "encore.backgroundConstructor", "Lp/f2v;-><init>(Lp/v4v;Lp/v4v;JJJ)V");
        add(targets, "encore.layerConstructor", "Lp/v4v;-><init>(JJJ)V");
        add(targets, "encore.textConstructor", "Lp/hnv;-><init>(JJJJJJJ)V");
        add(targets, "encore.essentialConstructor", "Lp/i7v;-><init>(JJJJJJJ)V");
        add(targets, "encore.decorativeConstructor", "Lp/q6v;-><init>(JJ)V");
        add(targets, "material.colorConstructor", "Lp/pnf;-><init>(JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ)V");
        TARGETS = Collections.unmodifiableMap(targets);
        add(targets, "album.legacyStartColor", "Lp/w730;-><init>(I)V");
        add(targets, "album.legacyEndColor", "Lp/x730;-><init>(I)V");
        add(targets, "album.legacyStop", "Lp/z730;-><init>(FLp/y730;)V");
        add(targets, "home.statusForeground", "Lcom/google/android/material/appbar/AppBarLayout;->getStatusBarForeground()Landroid/graphics/drawable/Drawable;");
        add(targets, "home.setStatusForeground", "Lcom/google/android/material/appbar/AppBarLayout;->setStatusBarForeground(Landroid/graphics/drawable/Drawable;)V");
        add(targets, "home.shortcutTitleOwner", "Lp/ur21;->E0(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        add(targets, "artwork.playerStateBuild", "Lcom/spotify/player/model/AutoValue_PlayerState$Builder;->build()Lcom/spotify/player/model/PlayerState;");
        add(targets, "artwork.currentTrack", "Lcom/spotify/player/model/PlayerState;->track()Lp/ovm0;");
        add(targets, "artwork.trackPresent", "Lp/ovm0;->c()Z");
        add(targets, "artwork.trackValue", "Lp/ovm0;->b()Ljava/lang/Object;");
        add(targets, "artwork.trackMetadata", "Lcom/spotify/player/model/ContextTrack;->metadata()Lp/e750;");
        Map<String, FieldTarget> fields = new LinkedHashMap<>();
        fields.put("album.legacyStopPosition", new FieldTarget("p.z730", "a", "float", false));
        fields.put("album.legacyStopColor", new FieldTarget("p.z730", "b", "p.y730", false));
        fields.put("home.shortcutVariant", new FieldTarget("p.ur21", "a", "int", false));
        fields.put("home.shortcutModel", new FieldTarget("p.ur21", "b", "p.hs21", false));
        fields.put("home.shortcutTitle", new FieldTarget("p.hs21", "a", "java.lang.String", false));
        fields.put("glyph.icon", new FieldTarget("p.bp51", "a", "p.dp51", false));
        fields.put("compose.viewLocal", new FieldTarget("androidx.compose.ui.platform.AndroidCompositionLocals_androidKt", "f", "p.ib61", true));
        fields.put("songDna.contentVariant", new FieldTarget("p.rw41", "a", "int", false));
        fields.put("creation.model", new FieldTarget("p.y8j", "c", "java.lang.Object", false));
        fields.put("library.title", new FieldTarget("p.lvy", "e", "java.lang.String", false));
        fields.put("playlist.titleVariant", new FieldTarget("p.plu0", "a", "int", false));
        fields.put("playlist.titleModel", new FieldTarget("p.plu0", "d", "java.lang.Object", false));
        fields.put("playlist.title", new FieldTarget("p.ew51", "b", "java.lang.String", false));
        fields.put("playlist.creatorText", new FieldTarget("p.b7m", "a", "p.u55", false));
        fields.put("compose.packedColor", new FieldTarget("p.glf", "a", "long", false));
        fields.put("lottie.colorFilterProperty", new FieldTarget("p.m4c0", "F", "android.graphics.ColorFilter", true));
        fields.put("saved.viewState", new FieldTarget("com.spotify.encoreconsumermobile.elements.addtobutton.AddToButtonView", "d", "p.lj1", false));
        fields.put("saved.encoreState", new FieldTarget("com.spotify.encoreconsumermobile.elements.addtobutton.EncoreAddToButtonView", "T0", "p.lj1", false));
        fields.put("saved.addedState", new FieldTarget("p.lj1", "a", "p.mj1", false));
        fields.put("glyph.colorState", new FieldTarget("p.bp51", "h", "android.content.res.ColorStateList", false));
        fields.put("glyph.paint", new FieldTarget("p.bp51", "g", "android.graphics.Paint", false));
        fields.put("transport.circleGlyph", new FieldTarget("p.bse", "h", "android.graphics.drawable.Drawable", false));
        fields.put("transport.circleColors", new FieldTarget("p.bse", "e", "android.content.res.ColorStateList", false));
        fields.put("transport.circlePaint", new FieldTarget("p.bse", "d", "android.graphics.Paint", false));
        fields.put("player.overlayDrawable", new FieldTarget("com.spotify.nowplaying.uiusecases.overlay.OverlayHidingGradientBackgroundView", "R0", "android.graphics.drawable.GradientDrawable", false));
        fields.put("home.filterChipColor", new FieldTarget("p.wv30", "b", "long", true));
        fields.put("biography.rowVariant", new FieldTarget("p.csp", "a", "int", false));
        fields.put("biography.rowOwner", new FieldTarget("p.csp", "c", "p.esp", false));
        fields.put("biography.rowView", new FieldTarget("p.esp", "g", "androidx.compose.ui.platform.ComposeView", false));
        fields.put("biography.followingInstance", new FieldTarget("p.txg", "i", "p.txg", true));
        fields.put("home.filterChipPaletteProvider", new FieldTarget("p.b9", "b", "java.lang.Object", false));
        fields.put("home.filterChipUnselected", new FieldTarget("p.wv30", "d", "p.k4v", true));
        fields.put("home.filterChipFirstProvider", new FieldTarget("p.k4v", "d", "p.b710", false));
        fields.put("home.filterChipSecondProvider", new FieldTarget("p.k4v", "e", "p.b710", false));
        fields.put("home.filterChipThirdProvider", new FieldTarget("p.k4v", "f", "p.b710", false));
        fields.put("artwork.encorePlaceholder", new FieldTarget("com.spotify.encoreconsumermobile.elements.artwork.ArtworkView", "d", "android.graphics.drawable.ColorDrawable", false));
        fields.put("artwork.encoreOverlay", new FieldTarget("com.spotify.encoreconsumermobile.elements.artwork.ArtworkView", "e", "p.xxd0", false));
        fields.put("artwork.encoreLayers", new FieldTarget("com.spotify.encoreconsumermobile.elements.artwork.ArtworkView", "h", "android.graphics.drawable.LayerDrawable", false));
        fields.put("artwork.creativePlaceholder", new FieldTarget("com.spotify.creativeworkplatform.encore.elements.ArtworkView", "d", "android.graphics.drawable.ColorDrawable", false));
        fields.put("artwork.creativeOverlay", new FieldTarget("com.spotify.creativeworkplatform.encore.elements.ArtworkView", "e", "android.graphics.drawable.ColorDrawable", false));
        fields.put("artwork.overlayFill", new FieldTarget("p.wxd0", "c", "android.content.res.ColorStateList", false));
        fields.put("artwork.overlayStroke", new FieldTarget("p.wxd0", "d", "android.content.res.ColorStateList", false));
        fields.put("artwork.overlayTint", new FieldTarget("p.wxd0", "e", "android.content.res.ColorStateList", false));
        fields.put("album.callbackOwner", new FieldTarget("p.drl", "b", "java.lang.Object", false));
        fields.put("row.root", new FieldTarget("p.oin", "a", "java.lang.Object", false));
        fields.put("countdown.ownerModel", new FieldTarget("p.cw5", "c", "java.lang.Object", false));
        fields.put("encore.backgrounds", new FieldTarget("p.u4v", "a", "p.f2v", false));
        fields.put("encore.backgroundBase", new FieldTarget("p.f2v", "c", "long", false));
        fields.put("encore.backgroundHighlight", new FieldTarget("p.f2v", "d", "long", false));
        fields.put("encore.backgroundPress", new FieldTarget("p.f2v", "e", "long", false));
        for (int i = 0; i < MATERIAL_COLOR_FIELDS.length(); i++) {
            fields.put("material.color." + i, new FieldTarget("p.pnf",
                    String.valueOf(MATERIAL_COLOR_FIELDS.charAt(i)), "long", false));
        }
        FIELDS = Collections.unmodifiableMap(fields);
    }

    private static void add(Map<String, MethodTarget> map, String role, String descriptor) {
        if (map.put(role, new MethodTarget(descriptor)) != null) throw new IllegalStateException("Duplicate role " + role);
    }

    private final ClassLoader loader;
    private final Map<String, Executable> resolved = new LinkedHashMap<>();
    private final Map<String, Field> resolvedFields = new LinkedHashMap<>();

    public Profile_9_1_86_2432(String versionName, long versionCode, ClassLoader loader) {
        if (!supports(versionName, versionCode)) throw new IllegalArgumentException("Unsupported Spotify version: " + versionName + " / " + versionCode);
        if (loader == null) throw new IllegalArgumentException("Spotify ClassLoader is required");
        this.loader = loader;
    }

    public static boolean supports(String versionName, long versionCode) {
        return VERSION_NAME.equals(versionName) && versionCode == VERSION_CODE;
    }

    public synchronized Executable resolve(String role) throws ReflectiveOperationException {
        MethodTarget target = TARGETS.get(role);
        if (target == null) throw new IllegalArgumentException("Unmapped profile role: " + role);
        Executable executable = resolved.get(role);
        if (executable == null) {
            executable = target.resolve(loader);
            resolved.put(role, executable);
        }
        return executable;
    }

    public static Map<String, MethodTarget> targets() { return TARGETS; }

    public synchronized Field field(String role) throws ReflectiveOperationException {
        FieldTarget target = FIELDS.get(role);
        if (target == null) throw new IllegalArgumentException("Unmapped field role: " + role);
        Field field = resolvedFields.get(role);
        if (field == null) {
            field = target.resolve(loader);
            resolvedFields.put(role, field);
        }
        return field;
    }
}
