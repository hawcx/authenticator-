import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class SignInScreen extends StatefulWidget {
  @override
  _SignInScreenState createState() => _SignInScreenState();
}

class _SignInScreenState extends State<SignInScreen> {
  static const platform = MethodChannel('com.example.flutter_hawcx/hawcx');
  final TextEditingController _emailController = TextEditingController();
  bool showManualSignIn =
      false; // Initially false, show after biometric login fails
  bool isBiometricInProgress = true; // Show loading spinner initially

  @override
  void initState() {
    super.initState();
    // Start biometric login as soon as the screen loads
    WidgetsBinding.instance.addPostFrameCallback((_) {
      handleBiometricLogin();
    });
  }

  Future<void> handleBiometricLogin() async {
    try {
      final String result = await platform.invokeMethod('checkLastUser');
      if (result.contains('Login successful for user')) {
        Navigator.pushReplacementNamed(context, '/home');
      } else {
        setState(() {
          showManualSignIn = true;
        });
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Biometric authentication failed')));
      }
    } catch (e) {
      print(e);
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Biometric login failed')));
      setState(() {
        showManualSignIn = true;
      });
    } finally {
      setState(() {
        isBiometricInProgress = false;
      });
    }
  }

  void handleManualSignIn() async {
    if (_emailController.text.isNotEmpty) {
      setState(() {
        showManualSignIn = false;
      });

      try {
        await platform
            .invokeMethod('signIn', {'username': _emailController.text});

        // After successful sign-in, call biometric login again
        await handleBiometricLogin();
      } on PlatformException catch (e) {
        if (e.message != null &&
            e.message!.contains('Restore your account on this device')) {
          Navigator.pushNamed(context, '/accountRestore');
        } else {
          ScaffoldMessenger.of(context)
              .showSnackBar(SnackBar(content: Text('Sign in failed')));
        }
      } catch (e) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text('Sign in failed')));
      } finally {
        setState(() {
          showManualSignIn = true;
        });
      }
    } else {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('Please enter your email')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Sign In')),
      body: isBiometricInProgress
          ? Center(child: CircularProgressIndicator())
          : Padding(
              padding: EdgeInsets.all(16),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  showManualSignIn
                      ? Column(
                          children: [
                            TextField(
                              controller: _emailController,
                              decoration: InputDecoration(labelText: 'Email'),
                              keyboardType: TextInputType.emailAddress,
                            ),
                            ElevatedButton(
                              onPressed: handleManualSignIn,
                              child: Text('Sign In'),
                            ),
                            SizedBox(height: 16),
                            // Restore Account link
                            TextButton(
                              onPressed: () {
                                Navigator.pushNamed(context, '/accountRestore');
                              },
                              child: Text('Restore Account'),
                            ),
                            // Signup link
                            TextButton(
                              onPressed: () {
                                Navigator.pushNamed(context, '/signup');
                              },
                              child: Text("Don't have an account? Sign Up"),
                            ),
                          ],
                        )
                      : Text(
                          'Attempting Biometric Login...',
                          style: TextStyle(fontSize: 16),
                        ),
                ],
              ),
            ),
    );
  }
}
