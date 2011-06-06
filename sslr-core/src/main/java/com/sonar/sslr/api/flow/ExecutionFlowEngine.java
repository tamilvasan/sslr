/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.api.flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sonar.sslr.api.AstNode;

public class ExecutionFlowEngine<STATEMENT extends Statement> implements ExecutionFlow<STATEMENT> {

  private ExecutionFlowVisitor<STATEMENT>[] visitors = new ExecutionFlowVisitor[0];
  private final FlowHandlerStack flowHandlerStack = new FlowHandlerStack();
  private STATEMENT lastStmt;
  private STATEMENT lastEndPathStmt;
  private STATEMENT firstStmt;
  private boolean executionFlowStarted = false;
  private Map<AstNode, STATEMENT> stmtAstNodes = new HashMap<AstNode, STATEMENT>();

  public final void add(STATEMENT stmt) {
    stmtAstNodes.put(stmt.getAstNode(), stmt);
  }

  public final STATEMENT getStatement(AstNode stmtNode) {
    return stmtAstNodes.get(stmtNode);
  }

  public final void visitFlow(AstNode stmtToStartVisitFrom, ExecutionFlowVisitor<STATEMENT>... visitors) {
    this.visitors = visitors;
    visitFlow(stmtToStartVisitFrom);
    start();
  }

  public final void visitFlow(STATEMENT stmtToStartVisitFrom, ExecutionFlowVisitor<STATEMENT>... visitors) {
    this.visitors = visitors;
    visitFlow(stmtToStartVisitFrom);
    start();
  }

  public final Collection<STATEMENT> getStatements() {
    return stmtAstNodes.values();
  }

  public void visitFlow(AstNode stmtToStartVisitFrom) {
    visitFlow(getStatement(stmtToStartVisitFrom));
  }

  public void visitFlow(STATEMENT stmtToStartVisitFrom) {
    if ( !executionFlowStarted) {
      this.firstStmt = stmtToStartVisitFrom;
      return;
    }
    STATEMENT currentStmt = stmtToStartVisitFrom;
    while (currentStmt != null) {
      lastStmt = currentStmt;
      callVisitStatementOnVisitors();
      if (currentStmt.hasFlowHandler()) {
        FlowHandler flowHandler = currentStmt.getFlowHandler();
        flowHandler.processFlow(this);
      }
      currentStmt = (STATEMENT) currentStmt.getNext();
    }

    if (firstStmt == stmtToStartVisitFrom) {
      callEndPathOnVisitors();
    }
  }

  public void callEndPathOnVisitors() {
    if (lastStmt != lastEndPathStmt) {
      for (int i = 0; i < visitors.length; i++) {
        visitors[i].endPath();
      }
    }
    lastEndPathStmt = lastStmt;
  }

  private void callVisitStatementOnVisitors() {
    for (int i = 0; i < visitors.length; i++) {
      visitors[i].visitStatement(lastStmt);
    }
  }

  public void callVisitBranchOnVisitors() {
    for (int i = 0; i < visitors.length; i++) {
      visitors[i].visitBranch();
    }
  }

  public void callVisitMandatoryBranches() {
    for (int i = 0; i < visitors.length; i++) {
      visitors[i].visitMandatoryBranches();
    }
  }

  public void callLeaveMandatoryBranches() {
    for (int i = 0; i < visitors.length; i++) {
      visitors[i].leaveMandatoryBranches();
    }
  }

  public void callLeaveBranchOnVisitors() {
    for (int i = 0; i < visitors.length; i++) {
      visitors[i].leaveBranch();
    }
  }

  void start() {
    executionFlowStarted = true;
    callStartOnVisitors();
    try {
      visitFlow(firstStmt);
    } catch (StopPathExplorationSignal signal) {
    } catch (StopFlowExplorationSignal signal) {
    } catch (BarrierSignal signal) {
    } finally {
      try {
        callEndPathOnVisitors();
      } catch (ExecutionFlowSignal signal) {
      }
      executionFlowStarted = false;
    }
    callStopOnVisitors();
  }

  private void callStopOnVisitors() {
    for (int i = 0; i < visitors.length; i++) {
      visitors[i].stop();
    }
  }

  private void callStartOnVisitors() {
    for (int i = 0; i < visitors.length; i++) {
      visitors[i].start();
    }
  }

  public FlowHandlerStack getFlowHandlerStack() {
    return flowHandlerStack;
  }

  public class FlowHandlerStack {

    private final Stack<FlowHandler> branches = new Stack<FlowHandler>();

    public final boolean isEmpty() {
      return branches.isEmpty();
    }

    public final void add(FlowHandler flowHandler) {
      branches.push(flowHandler);
    }

    public final FlowHandler peek() {
      return branches.peek();
    }

    public FlowHandler pop() {
      return branches.pop();
    }

    public boolean contains(FlowHandler flowHandler) {
      return branches.contains(flowHandler);
    }
  }

  public void visitFlow(ExecutionFlowVisitor<STATEMENT>... visitors) {
    throw new UnsupportedOperationException();
  }
}