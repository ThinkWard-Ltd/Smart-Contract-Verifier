import 'package:flutter/material.dart';
import 'package:unison/models/condition.dart';
import 'package:unison/models/global.dart';
import 'package:unison/services/Blockchain/unisonService.dart';
import 'package:unison/services/Server/judgeService.dart';
import 'package:unison/services/Server/negotiationService.dart';

import '../models/contract.dart';

class ContractDetailInfoPanel extends StatefulWidget {
  final Contract _contract;
  ContractDetailInfoPanel(this._contract);
  NegotiationService negotiationService = NegotiationService();
  UnisonService unisonService = UnisonService();

  @override
  _ContractDetailInfoPanelState createState() =>
      _ContractDetailInfoPanelState();
}

class _ContractDetailInfoPanelState extends State<ContractDetailInfoPanel> {
  bool disableSealButton;
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Card(
        elevation: 15,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            ListTile(
              leading: Icon(Icons.info),
              title: Text(widget._contract.contractId),
              subtitle: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Created: ${widget._contract.createdDate}'),
                  Text(
                      'Moved to Blockchain: ${widget._contract.movedToBlockchain.toString()}'),
                  SizedBox(
                    width: 15,
                  ),
                  _buildSealButton(),
                ],
              ),
            ),
            Container(
              padding: EdgeInsets.only(
                left: 15,
                right: 15,
                bottom: 10,
              ),
              width: double.infinity,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Agreement ID: ${widget._contract.contractId}'),
                  Text(
                    'Party A: ${widget._contract.partyA}',
                    style: TextStyle(
                      color: widget._contract.partyA == Global.userAddress
                          ? Colors.deepOrange
                          : Colors.cyan,
                    ),
                  ),
                  Text(
                    'Party B: ${widget._contract.partyB}',
                    style: TextStyle(
                      color: widget._contract.partyB == Global.userAddress
                          ? Colors.deepOrange
                          : Colors.cyan,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSealButton() {
    bool stillPending = false;
    String pendingMessage = 'Awaiting PENDING Conditions';
    widget._contract.conditions.forEach((condition) {
      if (condition.status == 'PENDING') {
        stillPending = true;
      }

      //TODO: Add a check for if a payment/duration condition have been set
    });
    if (widget._contract.conditions.isEmpty) {
      stillPending = true;
      pendingMessage = 'Please Add Conditions';
    }
    if (widget._contract.price == null) {
      stillPending = true;
      pendingMessage = 'Please Add a Payment Condition';
    }
    if (widget._contract.duration == null) {
      stillPending = true;
      pendingMessage = 'Please Add a Duration Condition';
    }
    ;

    return widget._contract.sealedDate != null
        ? Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Agreement Sealed'),
              SizedBox(
                height: 2,
              ),
              Row(
                children: [
                  ElevatedButton(
                    onPressed: () async {
                      try {
                        print('Happiness');
                        await widget.unisonService.agreementFulfilled(
                          widget._contract,
                          true,
                        );
                      } catch (error) {
                        print(error);
                        setState(() {});
                      }
                    },
                    child: Text('Conclude Agreement'),
                    style: ElevatedButton.styleFrom(
                      primary: Colors.green,
                      padding:
                          EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                    ),
                  ),
                  SizedBox(
                    width: 5,
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      try {
                        print('Not happiness');
                        await widget.unisonService.agreementFulfilled(
                          widget._contract,
                          false,
                        );
                      } catch (error) {
                        print(error);
                        setState(() {});
                      }
                    },
                    child: Text('Dispute Agreement'),
                    style: ElevatedButton.styleFrom(
                      primary: Colors.red,
                      padding:
                          EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                    ),
                  ),
                ],
              ),

              //Only the user who did not 'Seal Agreement' Should see this
              // button:
              SizedBox(
                height: 5,
              ),

              FutureBuilder(
                  future: widget.unisonService
                      .getAgreement(widget._contract.blockchainId),
                  builder: (context, snapshot) {
                    return snapshot.connectionState == ConnectionState.done
                        // ? Text('DONE ' + snapshot.data.serverID.toString())
                        ? snapshot.data.shouldAccept() == true
                            ? ElevatedButton(
                                onPressed: () async {
                                  try {
                                    print('Accept Blockchain Agreement');
                                    await widget.unisonService.acceptAgreement(
                                      widget._contract,
                                    );
                                  } catch (error) {
                                    print(error);
                                    setState(() {});
                                  }
                                },
                                child: Text('Accept Blockchain Agreement'),
                              )
                            : Text('Awaiting other party confirmation')
                        : CircularProgressIndicator();
                  }),

              SizedBox(
                height: 5,
              ),
              ElevatedButton(
                onPressed: () async {
                  try {
                    print('Pay Platform Fee');
                    await widget.unisonService.payPlatformFee(
                      widget._contract.blockchainId,
                    );
                  } catch (error) {
                    print(error);
                    setState(() {});
                  }
                },
                child: Text('Pay Platform Fee'),
              ),
            ],
          )
        :
        // /TRUE
        // Text('Agreement Sealed')
        // FALSE
        stillPending
            ? Text(pendingMessage)
            : ElevatedButton(
                onPressed: () async {
                  try {
                    await widget.negotiationService
                        .sealAgreement(widget._contract);

                    print('SEAL THAT DEAL');
                    setState(() {
                      stillPending = false;
                      widget._contract.sealedDate = DateTime.now();
                      widget._contract.movedToBlockchain = true;
                    });
                  } catch (error) {
                    print(error);
                    setState(() {});
                  }
                },
                child: Text('Seal Agreement'),
              );
  }
}