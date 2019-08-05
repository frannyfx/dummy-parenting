# dummy-parenting
An Android app which, when triggered by an AWS IoT button, records the last few minutes of audio and keeps recording for a specified length of time.
## What you need
- [A PubNub account](https://pubnub.com).
- [An AWS account](https://aws.amazon.com).
- [Android Studio](https://developer.android.com/studio).
- [One or more AWS IoT buttons](https://aws.amazon.com/iotbutton/).

## How to setup
### Setting up PubNub
- [Log into your PubNub account](https://dashboard.pubnub.com/login) and create a new "App".
- Click on your new app and save the "Demo Keyset" keys, which you will need to publish and subscribe to events.

### Setting up the AWS Lambda function
- [Log into the AWS Management Console](https://console.aws.amazon.com/console/home).
- Search for *"Lambda,"* which is Amazon's serverless service. This will allow us to connect our IoT buttons with code that we wrote.
- Create a new function, selecting *"Author from scratch,"* and the latest version of Node.js.
- Pay close attention to the AWS region you've selected, as you will need to know it to connect the button to the function.
- Go into the project folder, and zip up the contents of the *"lambda"* folder
- Go back to your Lambda function and find the *"Function code"* section. In the dropdown menu called *"Code entry type,"* select *"Upload a .zip file,"* and upload the file you've just zipped.
- Now, find the *"Environment variables"* section and set `PUBLISH_KEY` to your PubNub publish key, and `SUBSCRIBE_KEY` to your PubNub subscribe key.

### Setting up your buttons
- Download the AWS IoT app on your mobile device (iOS/Android) and open it.
- Setup the button on your Wi-Fi, **ensuring you have selected the correct AWS region**, and when it asks what you'd like to trigger when the button is pressed, select the Lambda function you just created.

### Building the app
- Open the Android project in Android Studio and compile it.
- Install the app on your Android phone.

**The app is now ready to be used.**

## Usage
- Open the app on your Android phone and grant it all the permissions it requests;
- Go into the settings and click *"Setup triggers."* This will allow you to link IoT buttons with your phone.
- Add a trigger by typing in the serial number of your IoT button.
- Now, go back into settings and enable *"Background audio recording."*
- If you'd like to only enable the recording within specific time ranges, turn on the *"Scheduled recording"* option in the settings and add new time slots by pressing the *"Customise schedule"* button.

**Congratulations, you're now ready to record some audio!**