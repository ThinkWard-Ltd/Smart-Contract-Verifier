import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:unison/models/condition.dart';

import 'http_exception.dart';

class Contract with ChangeNotifier {
  String contractId; //agreementID
  BigInt blockchainId; //TODO: remove ples
  String partyA;
  String partyB;
  DateTime createdDate;
  DateTime sealedDate;
  bool movedToBlockchain;
  List<Condition> conditions; //TODO Handle empty/initial conditions
  String title;

  String description;
  String payingUser;
  double price;
  String imageUrl;
  String partyBId;
  BigInt duration;

  Contract({
    this.contractId,
    this.partyA,
    this.partyB,
    this.createdDate,
    this.sealedDate,
    this.movedToBlockchain,
    this.conditions,
    this.title,
    this.description,
    this.price,
    this.imageUrl,
  });

  //JSON constructor. Uses response from getAgreement. RC: Should be revised
  Contract.fromJson(Map jsn) {
    //TODO: publicWalletID will be upper case soon
    contractId = jsn['AgreementID'];
    partyA = jsn['PartyA']['publicWalletID'];
    partyB = jsn['PartyB']['publicWalletID'];
    // createdDate = null;
    // sealedDate = null;
    //status = json['status'],

    //Try to get items that may not exist yet
    try {
      duration = BigInt.from(jsn['DurationCondition']['Amount']);
    } catch (_) {}
    try {
      price = jsn['PaymentCondition']['Amount'];
    } catch (_) {}
    try {
      createdDate = DateTime.parse(jsn['CreatedDate']);
      sealedDate = DateTime.parse(jsn['SealedDate']);
    } catch (_) {}

    try {
      blockchainId = BigInt.from(jsn['BlockChainID']);
      movedToBlockchain = true;
    } catch (error) {
      print('BC ID ERR: ' + error.toString());
      // blockchainId = BigInt.from(0);
      movedToBlockchain = false;
    }
    movedToBlockchain = jsn['MovedToBlockchain'];
    description = jsn['AgreementDescription'];
    imageUrl = jsn['AgreementImageURL'];
    partyBId = '';
    title = jsn['AgreementTitle'];
    conditions = (jsn['Conditions'])
        .map<Condition>((i) => Condition.fromJson(i))
        .toList();
  }

  Map<String, String> toJson() {
    return {
      //This is used in the initial save to the backend, hence not all fields being present.
    //  'PartyA': partyA,
      'PartyB': partyB,
      'AgreementTitle': title,
      'AgreementDescription': description,
      'AgreementImageURL': imageUrl,
    };
  }

  // Map<String, String> toJson() => {
  //       //This is used in the initial save to the backend, hence not all fields being present.
  //       'PartyA': partyA,
  //       'PartyB': partyB,
  //       'AgreementTitle': title,
  //       'AgreementDescription': description,
  //       'AgreementImageURL': imageUrl,
  //     };

  Map<String, String> toJsonChain() => {
        //This is used in the save to the blockchain.
        'PartyA': partyA,
        'PartyB': partyB,
        'AgreementTitle': title,
        'AgreementDescription': description,
        'AgreementImageURL': imageUrl,
      };

  void setBlockchainID(BigInt id) {
    blockchainId = id;
    movedToBlockchain = true;
  }

  void setDuration(BigInt d) {
    duration = d;
  }

  void _setFavValue(bool newValue) {
    // isFavorite = newValue;
    notifyListeners();
  }

  ///Generate a string of conditions to send to the blockchain upon agreement creation.
  String dataToChain() {

    String ret = title + '#' + description + '#{';
     for (Condition i in conditions) {
        ret += i.toChain();
     }

     ret += '}';
     return ret;
  }

 ///Generate an instance from the blockchain. This differs from a blockchainagreement, in that it should only be used to verify the state of conditions saved in the smart contract.
 Contract.fromChain(String data) {

 }

  String toString() {
    //A ToString method for debugging purposes
    String ret = 'ID: ' + contractId + '\n';
    ret += 'Party A: ' + partyA.toString() + '\n';
    ret += 'Party B: ' + partyB.toString() + '\n';

    return ret;
  }
}
