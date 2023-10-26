import * as React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {NativeStackScreenProps} from '@react-navigation/native-stack';

// react-native-screens에 에러가 나서 아래 구문을 추가해줌
import {enableScreens} from 'react-native-screens';
enableScreens();

// type 가져오기
import {RootStackParamList} from './src/types/path';

//page 가져오기
import MainPage from './src/pages/MainPage';
import PloggingPage from './src/pages/PloggingPage';
import CameraPage from './src/pages/CameraPage';

// 여기서는 RootStackParamList 안에 있는 타입 지정 안해주면 에러남~!꼭 넣을 것
const Stack = createNativeStackNavigator<RootStackParamList>();

function App() {
  return (
    <NavigationContainer>
      {/* initialRouteName 는 가장 처음 나타나는 화면을 의미한다 */}
      <Stack.Navigator
        initialRouteName="Main"
        // 아래 코드 넣으면 뒤로가기 바가 있는 헤더가 사라짐
        // screenOptions={{headerShown: false}}
      >
        <Stack.Screen name="Main" component={MainPage} />
        <Stack.Screen name="Plogging" component={PloggingPage} />
        <Stack.Screen name="Camera" component={CameraPage} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
export default App;
