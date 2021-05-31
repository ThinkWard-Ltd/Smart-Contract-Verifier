import 'package:flutter/material.dart';
import 'package:frontend/widget/circular_percentage_indicator_widget.dart';

class DashboardCard extends StatelessWidget {
  final int percentage;
  final String contractName;

  DashboardCard({required this.percentage, required this.contractName});

  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      color: Color.fromRGBO(79, 79, 79, 1),
      child: Column(
        children: [
          ListTile(
            leading: Icon(
              Icons.article_outlined,
              color: Colors.white,
            ),
            title: Text(
              contractName,
              style: TextStyle(color: Colors.white),
            ),
            subtitle: Text(
              'Current Status',
              style: TextStyle(color: Colors.white.withOpacity(0.6)),
            ),
          ),
          CircularPercentageIndicator(percentage),
          ButtonBar(
            alignment: MainAxisAlignment.end,
            children: [
              TextButton(
                onPressed: () {
                  // Perform some action
                },
                child: const Text("View"),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
