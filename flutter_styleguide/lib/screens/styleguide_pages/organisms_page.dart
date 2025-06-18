import 'package:flutter/material.dart';

class OrganismsPage extends StatelessWidget {
  const OrganismsPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Organisms'),
      ),
      body: const Center(
        child: Text('Organisms Page'),
      ),
    );
  }
} 