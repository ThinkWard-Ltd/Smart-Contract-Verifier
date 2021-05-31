import 'package:flutter/material.dart';
import 'package:frontend/widget/coming_soon_widget.dart';

class ActivityPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: Text('Activity Stream'),
      centerTitle: true,
      backgroundColor: Colors.teal,
    ),
    body: Column(
      children: [
        ComingSoon(),
      ],
    ),
  );
}