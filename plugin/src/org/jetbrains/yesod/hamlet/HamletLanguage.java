package org.jetbrains.yesod.hamlet;

/**
 * @author Leyla H
 */

import com.intellij.lang.Language;

public class HamletLanguage extends Language {
    public static final HamletLanguage INSTANCE = new HamletLanguage();

    public HamletLanguage() {
        super("Hamlet", "text/hamlet");
    }
}