/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.sslr.api.flow;

public class ExecutionFlowError extends RuntimeException {

  public ExecutionFlowError(String message) {
    super(message);
  }

}