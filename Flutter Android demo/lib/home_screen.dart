import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  static const platform = MethodChannel('com.example.flutter_hawcx/hawcx');
  String username = '';
  final List<Map<String, String>> notifications = [
    {'id': '1', 'text': 'New login from Android device on April 27, 2024'},
    {'id': '2', 'text': 'Security update available'},
    {'id': '3', 'text': 'Passwordless authentication now live'},
  ];

  @override
  void initState() {
    super.initState();
    getLastUser();
  }

  void getLastUser() async {
    try {
      final user = await platform.invokeMethod('getLastUser');
      setState(() {
        username = user;
      });
    } catch (e) {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Failed to retrieve user')));
    }
  }

  void handleSupportLink() async {
    const url = 'https://docs.hawcx.com/';
    if (await canLaunch(url)) {
      await launch(url);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Could not launch support link')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: Text('Home'),
      ),
      body: ListView(
        padding: EdgeInsets.all(16),
        children: [
          Container(
            padding: EdgeInsets.all(16),
            decoration: BoxDecoration(
              border: Border.all(color: Colors.white),
            ),
            child: Column(
              children: [
                Text(
                  username.isEmpty ? 'Hi User!' : 'Hi $username!',
                  style: TextStyle(color: Colors.white, fontSize: 18),
                ),
                SizedBox(height: 16),
                Image.asset('images/logo-white.png', height: 180),
                SizedBox(height: 16),
                Text(
                  'Keep your data secure with Hawcx',
                  style: TextStyle(color: Colors.white, fontSize: 18),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
          SizedBox(height: 16),
          Text(
            '✨ Explore Hawcx Features ✨',
            style: TextStyle(color: Colors.white, fontSize: 20),
            textAlign: TextAlign.center,
          ),
          SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _buildFeatureCard(
                  'Seamless Login', 'Access your account without passwords.'),
              _buildFeatureCard('Enhanced Security',
                  'Add extra layers of security to your account.'),
            ],
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _buildFeatureCard('Your Data',
                  'Monitor your login activities and security metrics.'),
              _buildFeatureCard('Mobile Integration',
                  'Seamlessly integrate with your mobile devices.'),
            ],
          ),
          SizedBox(height: 16),
          Text(
            '✨ Notifications ✨',
            style: TextStyle(color: Colors.white, fontSize: 20),
            textAlign: TextAlign.center,
          ),
          SizedBox(height: 16),
          ...notifications.map((notification) {
            return Container(
              padding: EdgeInsets.all(16),
              margin: EdgeInsets.symmetric(vertical: 8),
              decoration: BoxDecoration(
                color: Colors.grey[800],
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text(
                notification['text']!,
                style: TextStyle(color: Colors.white),
              ),
            );
          }).toList(),
          SizedBox(height: 16),
          GestureDetector(
            onTap: handleSupportLink,
            child: Container(
              padding: EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.grey[900],
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text(
                'Need Help? Visit our Support Center',
                style: TextStyle(color: Colors.white, fontSize: 16),
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFeatureCard(String title, String description) {
    return Container(
      width: MediaQuery.of(context).size.width * 0.45,
      padding: EdgeInsets.all(16),
      margin: EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Colors.grey[800],
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(
                color: Colors.blueAccent,
                fontSize: 16,
                fontWeight: FontWeight.bold),
          ),
          SizedBox(height: 8),
          Text(
            description,
            style: TextStyle(color: Colors.white, fontSize: 14),
          ),
        ],
      ),
    );
  }
}
