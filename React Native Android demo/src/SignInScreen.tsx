import React, { useEffect, useState } from 'react';
import { View, Text, Button, Alert, TextInput, StyleSheet, TouchableOpacity, ActivityIndicator } from 'react-native';
import { NativeModules } from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';

const { HawcxModule } = NativeModules;

// Define your stack param list
type RootStackParamList = {
  SignIn: undefined;
  Home: undefined;
  Signup: undefined;
  AccountRestore: undefined;
};

// Define the props for the screen
type SignInScreenNavigationProp = StackNavigationProp<RootStackParamList, 'SignIn'>;
type SignInScreenRouteProp = RouteProp<RootStackParamList, 'SignIn'>;

type Props = {
  navigation: SignInScreenNavigationProp;
  route: SignInScreenRouteProp;
};

const SignInScreen: React.FC<Props> = ({ navigation }) => {
  const [email, setEmail] = useState('');
  const [showManualSignIn, setShowManualSignIn] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Trigger biometric login when the component mounts
    checkLastUser();
  }, []);

  const checkLastUser = () =>{
    setLoading(true);
    HawcxModule.checkLastUser()
    .then((result: string) => {
      if (result.includes('Login successful for user')) {
        // If the user is successfully logged in, navigate to Home
        setLoading(false);
        navigation.replace('Home');
      } else {
        // If biometric authentication fails, show manual sign-in form
        setLoading(false);
        setShowManualSignIn(true);
      }
    })
    .catch((error: any) => {
      // If biometric fails, show the manual sign-in form
      Alert.alert('Biometric Login Failed');
      setLoading(false);
      setShowManualSignIn(true);
    });
  }

  const handleManualSignIn = () => {
    setLoading(true);
    // Call your backend or authentication API for manual sign-in
    if (email) {
      // Call the signIn method from HawcxModule or a separate API
      HawcxModule.signIn(email)
        .then(() => {
          // Navigate to home if login is successful
          checkLastUser();
        })
        .catch((error: any) => {
          setLoading(false);
          Alert.alert('Sign In Error', error.message || 'Failed to sign in');
        });
    } else {
      setLoading(false);
      Alert.alert('Error', 'Please enter your email');
    }
  };

  return (
    <View style={styles.container}>
      {loading ? (
        <View style={styles.loaderContainer}>
          <ActivityIndicator size="large" color="#ffffff" />
          <Text style={styles.text}>Loading...</Text>
        </View>
      ) : showManualSignIn ? (
        <>
          <Text style={styles.title}>Sign In</Text>
          <TextInput
            style={styles.input}
            placeholder="Email"
            placeholderTextColor="gray"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
          />
          <Button title="Sign In" onPress={handleManualSignIn} />

          {/* Link to navigate to Signup Screen */}
          <TouchableOpacity onPress={() => navigation.navigate('Signup')}>
            <Text style={styles.link}>Don't have an account? Sign Up</Text>
          </TouchableOpacity>

          {/* Link to navigate to AccountRestore Screen */}
          <TouchableOpacity onPress={() => navigation.replace('AccountRestore')}>
            <Text style={styles.link}>Restore your account</Text>
          </TouchableOpacity>
        </>
      ) : (
        <Text style={styles.text}>Attempting Biometric Login...</Text>
      )}
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
  loaderContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
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

export default SignInScreen;
