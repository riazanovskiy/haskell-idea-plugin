package org.jetbrains.yesod.hamlet.psi;

/**
 * @author Leyla H
 */

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

public class Interpolation extends ASTWrapperPsiElement{
    public Interpolation (ASTNode node) {
        super(node);
    }
}