//This is for the ui to request data from the api in an abstract sense.
//This one deals with negotiation-related issues.

import 'dart:async';

import '../../models/condition.dart';
import '../../models/contract.dart';
import '../../models/global.dart';
import 'backendAPI.dart';

class NegotiationService {
  ApiInteraction _api = ApiInteraction();
  final String _reqPath = '/negotiation/';

  Future<dynamic> getNotifications(String party) async {
    return;
  }

  Future<void> saveAgreement(Contract agr) async {
    //Save initial version of agreement to backend
    //TODO list:
    //Add exception handling. Maybe change return type to String

    Map<String, dynamic> response;
    try {
      response =
          await _api.postData(_reqPath + 'create-agreement', agr.toJson());

      if (response['Status'] != 'SUCCESSFUL')
        throw Exception('Agreement could not be saved');
    } on Exception catch (e) {
      //Handle exception
      print(e);
      throw e;
    }
  }

  Future<void> saveCondition(Condition cond) async {
    //Save a condition associated with a contract

    Map<String, dynamic> response;
    try {
      response =
          await _api.postData(_reqPath + 'create-condition', cond.toJson());

      if (response['Status'] != 'SUCCESSFUL')
        throw Exception('Condition could not be saved');
    } on Exception catch (e) {
      //Handle exception
      print(e);
      throw e;
    }
  }

  void acceptCondition(String id) async {
    //Or condition object?

    await _handleCondition(id, true);
  }

  void rejectCondition(String id) async {
    await _handleCondition(id, false);
  }

  Future<void> _handleCondition(String id, bool acc) async {
    //Either accept or reject condition

    String path = acc ? 'accept-condition' : 'reject-condition';

    Map<String, dynamic> response;
    try {
      response = await _api.postData(_reqPath + path, {'ConditionID': id});

      if (response['Status'] != 'SUCCESSFUL')
        throw Exception(
            'Condition could not be ' + (acc ? 'accepted' : 'rejected'));
    } on Exception catch (e) {
      //Handle exception
      print(e);
      return;
    }
  }

  Future<void> setPayment(String con, double price) async {
    //Set the payment condition of an agreement.

    await _handlePayDuration(con, price, true);
  }

  Future<void> setDuration(String con, double dur) async {
    //Set the duration condition of an agreement.

    await _handlePayDuration(con, dur, false);
  }

  Future<void> _handlePayDuration(String con, double val, bool price) async {
    //Handles both price and duration

    Map<String, dynamic> body = {
      'ProposedUser': Global.userAddress,
      'AgreementID': con,
      (price ? 'Payment' : 'Duration'): val
    };
    Map<String, dynamic> response;
    String path = price ? 'payment' : 'duration';

    try {
      response = await _api.postData(_reqPath + 'set-$path-condition', body);

      if (response['Status'] != 'SUCCESSFUL')
        throw Exception(
            '${(price ? 'Payment' : 'Duration')} could not be saved');
    } on Exception catch (e) {
      //Handle exception
      print(e);
      throw e;
    }
  }

  Future<void> sealAgreement(Contract con) async {
    //Or pass in Contract?
    //RFC: Should the blockchain be called immediately after the backend?

    Map<String, dynamic> response;
    try {
      response = await _api.postData(
          _reqPath + 'seal-agreement', {'AgreementID': con.contractId});

      if (response['Status'] != 'SUCCESSFUL')
        throw Exception('Agreement could not be sealed');
    } on Exception catch (e) {
      //Handle exception
      print(e);
      return;
    }
  }
}
