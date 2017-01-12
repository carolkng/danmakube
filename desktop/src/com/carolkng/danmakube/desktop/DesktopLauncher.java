package com.carolkng.danmakube.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.carolkng.danmakube.Danmakube;

public class DesktopLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "DanmaKUBE";
        config.width = 480;
        config.height = 800;
        new LwjglApplication(new Danmakube(), config);
    }
}
