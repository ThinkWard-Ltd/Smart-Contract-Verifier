import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/http_exception.dart';
import 'package:flutter/material.dart';

class Contract with ChangeNotifier {
  final String contractId; //agreementID
  final String durationId;
  String paymentId;
  final String partyA;
  final String partyB;
  final String createdDate;
  String sealedDate;
  bool movedToBlockchain;
  List<dynamic> conditions = ['empty']; //TODO

  final String title;
  final String description;
  final double price;
  final String imageUrl;
  final String partyBId;
  bool isFavorite;
  ContractColor color;

  Contract({
    this.contractId,
    this.durationId,
    this.paymentId,
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
    this.partyBId, //RC: What is partyB vs partyBID?
    this.isFavorite = false,
    this.color = ContractColor.cyan,
  }); // Contract({
  //   @required this.contractId,
  //   @required this.title,
  //   @required this.description,
  //   @required this.price,
  //   @required this.imageUrl,
  //   @required this.partyBId,
  //   this.isFavorite = false,
  // }); //not final because it will be changing

  //JSON constructor. Uses response from getAgreement. RC: Should be revised
  Contract.fromJson(Map<String, dynamic> json)
      : contractId = json['agreementID'],
        //duration = json['duration'], //Should be durationID?
        durationId = json['duration'],
        partyA = json['PartyA'],
        partyB = json['PartyB'],
        createdDate = json['createdDate'],
        sealedDate = json['sealedDate'],
        //status = json['status'],
        movedToBlockchain = json['movedToBlockChain'],
        description = '',
        imageUrl = '',
        partyBId = '',
        price = 0,
        title = '',
        conditions = json['conditions'];

  void _setFavValue(bool newValue) {
    isFavorite = newValue;
    notifyListeners();
  }

  //RC: Could this be stored locally rather than on the db? Since it's a user preference
  Future<void> toggleFavoriteStatus(String token, String userId) async {
    final oldStatus = isFavorite;
    _setFavValue(!isFavorite);
    final url =
        'https://capstone-testing-a7ee4-default-rtdb.firebaseio.com/userFavorites/$userId/$contractId.json?auth=$token';
    try {
      final response = await http.put(url,
          body: json.encode(
            isFavorite,
          ));
      if (response.statusCode >= 400)
        throw HttpException('Couldn\'t toggle favorite');
    } catch (error) {
      _setFavValue(oldStatus);
    }
  }
}

enum ContractColor {
  cyan,
  deepOrange,
  pink,
  purple,
  green,
}

extension ContractColorExtension on ContractColor {
  Color get name {
    switch (this) {
      case ContractColor.cyan:
        return Colors.cyan;
      case ContractColor.deepOrange:
        return Colors.deepOrange;
      case ContractColor.pink:
        return Colors.pink;
      case ContractColor.purple:
        return Colors.purple;
      case ContractColor.green:
        return Colors.green;
      default:
        return Colors.cyan;
    }
  }
}
