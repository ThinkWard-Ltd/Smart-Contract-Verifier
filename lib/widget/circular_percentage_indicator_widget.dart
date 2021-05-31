import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:percent_indicator/circular_percent_indicator.dart';

class CircularPercentageIndicator extends StatelessWidget {
  final int percentage;

  CircularPercentageIndicator(this.percentage);

  @override
  Widget build(BuildContext context) {
    return Container(
        padding: EdgeInsets.all(0),
        child: CircularPercentIndicator(
          radius: 80.0,
          lineWidth: 6.0,
          animation: true,
          percent: percentage / 100,
          center: Text(
            percentage.toString() + "%",
            style: TextStyle(
              fontSize: 20.0,
              fontWeight: FontWeight.w600,
              color: Colors.teal,
            ),
          ),
          backgroundColor: Color.fromRGBO(105, 105, 105, 1),
          circularStrokeCap: CircularStrokeCap.round,
          progressColor: Colors.tealAccent,
        ));
  }
}
