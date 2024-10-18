import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, TouchableOpacity } from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';
import { NativeModules } from 'react-native';

// Access HawcxModule from NativeModules
const { HawcxModule } = NativeModules;

// Define your stack param list
type RootStackParamList = {
  Signup: undefined;
  Verification: { email: string };
  SignIn: undefined;
  Home: undefined; // Add Home route for navigation on successful login
};

type SignupScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Signup'>;
type SignupScreenRouteProp = RouteProp<RootStackParamList, 'Signup'>;

type Props = {
  navigation: SignupScreenNavigationProp;
  route: SignupScreenRouteProp;
};

const SignupScreen: React.FC<Props> = ({ navigation }) => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);

  // Check if a user exists on component mount and trigger biometric login if a user exists
  useEffect(() => {
    setLoading(true);
    HawcxModule.checkLastUser()
      .then((result: string) => {
        if (result.includes('Login successful for user')) {
          // Navigate to Home if biometric login is successful
          setLoading(false);
          navigation.replace('Home');
        } else if (result === 'SHOW_EMAIL_SIGN_IN_SCREEN') {
          // If user exists but needs to sign in manually, navigate to the SignIn screen
          setLoading(false)
          navigation.navigate('SignIn');
        }
      })
      .catch((error: any) => {
        setLoading(false);
        console.error('Error checking last user', error);
      });
  }, []);

  const handleSignup = () => {
    setLoading(true);
    // Call the signUp method from HawcxModule
    HawcxModule.signUp(email)
      .then(() => {
        // Navigate to Verification screen if OTP is successfully generated
        navigation.navigate('Verification', { email });
        setLoading(false);
      })
      .catch((error: any) => {
        // Show an error message if signup fails
        Alert.alert('Signup Error', error.message || 'An error occurred');
        setLoading(false);
      });
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Sign Up</Text>
      <Text>Enter your Email to Sign Up:</Text>
      <TextInput
        style={styles.input}
        placeholder="Email"
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
      />
      <Button title={loading ? 'Signing Up...' : 'Sign Up'} onPress={handleSignup} disabled={loading} />

      {/* Link to navigate to SignIn Screen */}
      <TouchableOpacity onPress={() => navigation.navigate('SignIn')}>
        <Text style={styles.link}>Already have an account? Sign In</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 16,
    color: 'white',
    backgroundColor: 'black',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    color: 'white',
    marginBottom: 20,
  },
  input: {
    borderWidth: 1,
    padding: 10,
    marginVertical: 12,
    width: '100%',
    borderRadius: 4,
    borderColor: 'gray',
    color: 'white',
    backgroundColor: 'black',
  },
  link: {
    color: 'white',
    marginTop: 10,
    textAlign: 'center',
  },
  text: {
    textAlign: 'center',
    color: 'white',
  },
});

export default SignupScreen;
