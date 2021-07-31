//This class is used for interaction with the deployed server backend.
//String baseUrl is the root url of the server.
//When using these functions, the url passed in is effectively the api endpoint, an extension to the root url.

import 'dart:convert';
import 'package:retry/retry.dart';
import 'package:http/http.dart';
import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:io';

class ApiInteraction {

  final String baseUrl = "http://localhost:8080"; //Url where the backend is deployed

  Future<Map<dynamic, dynamic>> getData(String url) async{ //Pass in url extension
    var response;
    try {
      response = await RetryOptions(maxAttempts: 5).retry(
            () => get(baseUrl + url).timeout(Duration(seconds: 5)),
        retryIf: (e) => e is SocketException || e is TimeoutException,
      );
    } on Exception catch(e) { //Some other exception
      throw Exception('Could not connect to backend'); //Could be expanded in the future
    }

    if (response.statusCode != 200)
      throw Exception('Request could not be made'); //Failed http request

    //print (response.body);
    return json.decode(response.body);

  }

  Future<Map<String, dynamic>> postData(String url, Map<dynamic, dynamic> jsn) async{ //Pass in url extension and json body
    var response;

    try {
      response = await RetryOptions(maxAttempts: 5).retry(
            () =>
            post(baseUrl + url, headers: <String, String>{
              'Content-Type': 'application/json; charset=UTF-8',
            },
            body: jsonEncode(jsn)).timeout(Duration(seconds: 1)),
        retryIf: (e) => e is SocketException || e is TimeoutException,
      );
    } on Exception catch(e) {
      print ('Error: ' + e.toString());
      throw Exception('Could not connect to backend'); //Could be expanded in the future
    }

    if (response.statusCode != 200)
      throw Exception('An error occurred while making the request. The server responded with status code ' + response.statusCode.toString()); //Failed http request

    print (response.body);
    return json.decode(response.body.toString());
  }

}