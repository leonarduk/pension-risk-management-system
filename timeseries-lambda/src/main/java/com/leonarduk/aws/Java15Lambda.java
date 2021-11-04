package com.leonarduk.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Java15Lambda implements RequestHandler<Void, String> {
 
  @Override
  public String handleRequest(Void input, Context context) {

    String message = "mmmm";
//    var message = """
//      Hello World!
//
//      I'm using one of the latest language feature's of Java.
//      That's cool, isn't it?
//
//      Kind regards,
//      Duke
//      """;
 
    return message;
  }
}