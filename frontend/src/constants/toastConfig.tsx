import Toast, {BaseToast, ErrorToast} from "react-native-toast-message";
import { windowHeight } from "./styles";

export const toastConfig = {
  success: (props: any) => (
    <BaseToast
      {...props}
      style={{backgroundColor: '#34D399', borderLeftColor: '#34D399'}}
      text1Style={{fontSize: 20, fontFamily: 'NPSfont_bold', color: 'white'}}
    />
  ),
  error: (props: any) => (
    <ErrorToast
      {...props}
      style={{backgroundColor: '#D94040', borderLeftColor: '#D94040'}}
      text1Style={{fontSize: 20, fontFamily: 'NPSfont_bold', color: 'white'}}
    />
  ),
  end: (props: any) => (
    <BaseToast
      {...props}
      style={{backgroundColor: '#2C9261', borderLeftColor: '#2C9261', justifyContent: 'center', alignItems: 'center', zIndex: 100, position: 'absolute', top: windowHeight*0.02,}}
      text1Style={{fontSize: 20, fontFamily: 'NPSfont_bold', color: 'white'}}
    />
  )
}