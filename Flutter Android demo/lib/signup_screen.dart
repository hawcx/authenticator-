import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // For Native MethodChannel calls
import 'package:email_validator/email_validator.dart'; // Optional email validation package

class SignupScreen extends StatefulWidget {
  @override
  _SignupScreenState createState() => _SignupScreenState();
}

class _SignupScreenState extends State<SignupScreen> {
  static const platform = MethodChannel(
      'com.example.flutter_hawcx/hawcx'); // Similar to NativeModules in React Native
  final TextEditingController _emailController = TextEditingController();
  bool _loading = false; // Loading state to manage the signup process

  @override
  void initState() {
    super.initState();
    checkLastUser(); // Similar to useEffect in React Native
  }

  // Email validation
  bool validateEmail(String email) {
    return EmailValidator.validate(email);
  }

  // Check if a user exists and handle biometric login
  Future<void> checkLastUser() async {
    setState(() {
      _loading = true; // Show loading indicator
    });

    try {
      final String result = await platform.invokeMethod('checkLastUser');
      print(result);
      if (result.contains("Login successful for user")) {
        setState(() {
          _loading = false; // Hide loading indicator
        });
        Navigator.pushReplacementNamed(context, '/home');
      } else if (result == 'SHOW_EMAIL_SIGN_IN_SCREEN') {
        setState(() {
          _loading = false; // Hide loading indicator
        });
        Navigator.pushReplacementNamed(context, '/signin');
      }
    } on PlatformException catch (e) {
      print("Error checking last user: ${e.message}");
      setState(() {
        _loading = false; // Hide loading indicator
      });
    }
  }

  // Handle signup process
  void handleSignup() async {
    String email = _emailController.text.trim();
    if (email.isNotEmpty && validateEmail(email)) {
      setState(() {
        _loading = true; // Show loading state
      });
      try {
        await platform.invokeMethod('signUp', {'username': email});
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('OTP sent for verification!')));
        Navigator.pushNamed(context, '/verification', arguments: email);
      } on PlatformException catch (e) {
        // Check if the error message contains 'User already exists'
        if (e.message != null && e.message!.contains('User already exists')) {
          ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('User already exists. Please sign in.')));
          Navigator.pushNamed(context, '/signin');
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Sign up failed: ${e.message}')));
        }
      } catch (e) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text('Sign up failed')));
      } finally {
        setState(() {
          _loading = false; // Hide loading state
        });
      }
    } else if (email.isEmpty) {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Please enter your email')));
    } else {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Please enter a valid email')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Sign Up')),
      body: Stack(
        children: [
          Padding(
            padding: EdgeInsets.all(16),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  'Enter your Email to Sign Up:',
                  style: TextStyle(fontSize: 18),
                ),
                SizedBox(height: 12),
                TextField(
                  controller: _emailController,
                  decoration: InputDecoration(
                    labelText: 'Email',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.emailAddress,
                ),
                SizedBox(height: 12),
                ElevatedButton(
                  onPressed: _loading ? null : handleSignup,
                  child: Text(_loading ? 'Signing Up...' : 'Sign Up'),
                ),
                SizedBox(height: 12),
                TextButton(
                  onPressed: () => Navigator.pushNamed(context, '/signin'),
                  child: Text('Already have an account? Sign In'),
                ),
              ],
            ),
          ),
          // Show a circular progress indicator when _loading is true
          if (_loading)
            Center(
              child: CircularProgressIndicator(),
            ),
        ],
      ),
    );
  }
}
