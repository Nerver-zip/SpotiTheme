package com.spotitheme.hooks;

import android.view.View;
import android.view.ViewParent;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

/** Resource-owned rendering scopes shared by player hooks. */
final class ViewScopes {
    private ViewScopes() {}

    static String resource(View view) {
        if (view == null || view.getId() == View.NO_ID || view.getId() == 0) return "";
        try { return view.getResources().getResourceEntryName(view.getId()); }
        catch (android.content.res.Resources.NotFoundException ignored) { return ""; }
    }

    static boolean miniBar(View view) {
        String name = resource(view);
        return name.equals("now_playing_view_container")
                || name.equals("now_playing_mini_container")
                || name.equals("now_playing_bar_layout");
    }

    static boolean miniAncestor(View view) {
        ViewParent parent = view == null ? null : view.getParent();
        while (parent instanceof View) {
            if (miniBar((View) parent)) return true;
            parent = parent.getParent();
        }
        return false;
    }

    static View connectHost(View view) {
        for (View current = view; current != null;) {
            String name = resource(current);
            if (name.equals("connect_destination_button") || name.equals("connect_entry_point_stub"))
                return miniAncestor(current) || expandedPlayer(current) ? current : null;
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return null;
    }

    static boolean ancestor(View view, String name) {
        ViewParent parent = view == null ? null : view.getParent();
        while (parent instanceof View) {
            if (resource((View) parent).equals(name)) return true;
            parent = parent.getParent();
        }
        return false;
    }

    static boolean expandedPlayer(View view) {
        if (view == null) return false;
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) break;
            Context next = ((ContextWrapper) context).getBaseContext();
            if (next == context) break;
            context = next;
        }
        return context instanceof Activity
                && context.getClass().getName().equals("com.spotify.nowplaying.musicinstallation.NowPlayingActivity");
    }

    static boolean playerBackgroundOwner(View view) {
        return miniBar(view) || expandedPlayer(view) && resource(view).equals("revised_template_sticky_header");
    }
}
