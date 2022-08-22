package com.example;

public class FunctionOutput {
  private final String signedSoapMessage;

  public FunctionOutput(String signedSoapMessage) {
    this.signedSoapMessage = signedSoapMessage;
  }

  public String getSignedSoapMessage() {
    return signedSoapMessage;
  }
}
