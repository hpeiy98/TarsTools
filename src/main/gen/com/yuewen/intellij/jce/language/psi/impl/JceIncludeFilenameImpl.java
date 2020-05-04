// This is a generated file. Not intended for manual editing.
package com.yuewen.intellij.jce.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.yuewen.intellij.jce.language.psi.JceTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.yuewen.intellij.jce.language.psi.*;
import com.intellij.psi.PsiReference;

public class JceIncludeFilenameImpl extends ASTWrapperPsiElement implements JceIncludeFilename {

  public JceIncludeFilenameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JceVisitor visitor) {
    visitor.visitIncludeFilename(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JceVisitor) accept((JceVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getStringLiteral() {
    return findNotNullChildByType(STRING_LITERAL);
  }

  @Override
  @NotNull
  public String getFileName() {
    return JcePsiImplUtil.getFileName(this);
  }

  @Override
  public JceIncludeFilename setName(String newName) {
    return JcePsiImplUtil.setName(this, newName);
  }

  @Override
  @Nullable
  public PsiReference getReference() {
    return JcePsiImplUtil.getReference(this);
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return JcePsiImplUtil.getReferences(this);
  }

}
