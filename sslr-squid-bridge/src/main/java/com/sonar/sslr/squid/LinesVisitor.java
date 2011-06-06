/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.squid;

import static com.sonar.sslr.api.GenericTokenType.EOF;

import org.sonar.squid.measures.MetricDef;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;

/**
 * Visitor that computes the number of lines of a file.
 */
public class LinesVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private final MetricDef metric;
  private final SquidAstVisitorContext<? extends Grammar> context;

  public LinesVisitor(SquidAstVisitorContext<? extends Grammar> context, MetricDef metric) {
    this.context = context;
    this.metric = metric;
  }

  /**
   * {@inheritDoc}
   */
  public void visitToken(Token token) {
    if (token.getType() == EOF) {
      context.peekSourceCode().setMeasure(metric, token.getLine());
    }
  }

}