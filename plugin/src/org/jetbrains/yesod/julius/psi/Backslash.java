package org.jetbrains.yesod.julius.psi;

/**
 * @author Leyla H
 */

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

public class Backslash extends ASTWrapperPsiElement {
    public Backslash(ASTNode node) {
        super(node);
    }
}