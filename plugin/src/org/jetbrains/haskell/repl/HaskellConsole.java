package org.jetbrains.haskell.repl;

import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.openapi.project.Project;
import org.jetbrains.haskell.fileType.HaskellFileType;

public final class HaskellConsole extends LanguageConsoleImpl {
    public HaskellConsole(Project project,
                   String title) {
        super(project, title, HaskellFileType.INSTANCE.getLanguage());
    }

}
