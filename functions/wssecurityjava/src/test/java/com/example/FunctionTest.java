package com.example;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.Org;
import com.salesforce.functions.jvm.sdk.data.Record;
import com.salesforce.functions.jvm.sdk.data.RecordQueryResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FunctionTest {

  @Test
  public void test() throws Exception {
    WssecurityjavaFunction function = new WssecurityjavaFunction();
    FunctionOutput functionOutput = function.apply(createEventMock(), createContextMock());
    String actualOutput = functionOutput.getSignedSoapMessage();

    Assert.assertNotNull(actualOutput);
  }

  private Context createContextMock() {
    Context mockContext = mock(Context.class);

    when(mockContext.getOrg()).then(i1 -> {
      Org mockOrg = mock(Org.class, Mockito.RETURNS_DEEP_STUBS);

      when(mockOrg.getDataApi().query("SELECT Id, Name FROM Account")).then(i2 -> {
        RecordQueryResult mockResult = mock(RecordQueryResult.class);

        Record firstRecord = mock(Record.class);
        when(firstRecord.getStringField("Id")).thenReturn(Optional.of("5003000000D8cuIQAA"));
        when(firstRecord.getStringField("Name")).thenReturn(Optional.of("Account One, inc."));

        Record secondRecord = mock(Record.class);
        when(secondRecord.getStringField("Id")).thenReturn(Optional.of("6003000000D8cuIQAA"));
        when(secondRecord.getStringField("Name")).thenReturn(Optional.of("Account Two, inc."));

        when(mockResult.getRecords()).thenReturn(Arrays.asList(firstRecord, secondRecord));

        return mockResult;
      });

      return Optional.of(mockOrg);
    });

    return mockContext;
  }

  private InvocationEvent<FunctionInput> createEventMock() {
    InvocationEvent<FunctionInput> eventMock = mock(InvocationEvent.class);

    String mockXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<soapenv:Envelope " +
        "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "xmlns:urn=\"urn:com:nicolasvandenbossche:person:v1\">" +
        "<soapenv:Body>" +
        "<urn:GetIndividual User=\"Nicolas\">" +
        "<urn:IndividualId>8A89800663ED11C70163EF86F7BE64E7</urn:IndividualId>" +
        "</urn:GetIndividual>" +
        "</soapenv:Body>" +
        "</soapenv:Envelope>";
    when(eventMock.getData()).thenReturn(new FunctionInput(mockXml));

    return eventMock;
  }
}
