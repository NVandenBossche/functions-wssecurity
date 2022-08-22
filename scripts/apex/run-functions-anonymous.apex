// Anonymous Apex for testing Functions callout

functions.Function securityFunc = functions.Function.get('functionswssecurity.wssecurityjava');
String jsonInput = '{ "xmlBody" : "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?><soapenv:Envelope xmlns:soapenv=\\"http://schemas.xmlsoap.org/soap/envelope/\\" xmlns:urn=\\"urn:com:nicolasvandenbossche:person:v1\\"><soapenv:Body><urn:GetIndividual User=\\"Nicolas\\"><urn:IndividualId>8A89800663ED11C70163EF86F7BE64E7</urn:IndividualId></urn:GetIndividual></soapenv:Body></soapenv:Envelope>" }';
System.debug('Function input: ' + jsonInput);

functions.FunctionInvocation invocation = securityFunc.invoke(jsonInput);
String jsonResponse = invocation.getResponse();
System.debug('Function output: ' + jsonResponse);

Map<String, Object> xmlResponse = (Map<String, Object>) JSON.deserializeUntyped(jsonResponse);
System.debug('Xml retrieved: ' + xmlResponse.get('signedSoapMessage'));