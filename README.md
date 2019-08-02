# dummy-parenting
An Android app which, when triggered by an AWS IoT button, records the last few minutes of audio and keeps recording for a specified length of time.
## What you need
- A PubNub account;
- An AWS account;
- Android Studio;
- One or more AWS IoT buttons;

## How to setup
### PubNub
- Log into your PubNub account and create a new "App";
- Click on your new app and save the "Demo Keyset" keys, which you will need to publish and subscribe to events;

### AWS Lambda
- Log into the AWS Management Console;
- Search for "Lambda", which is Amazon's serverless service. This will allow us to connect our IoT buttons with code that we wrote;
- Create a new function, selecting "Author from scratch", and the latest version of Node.js;
- Pay close attention to the AWS region you've selected, as you will need to know it to connect the button to the function;
- Go into the project folder, and zip up the contents of the "lambda" folder;
- Go back to your Lambda function and find the "Function code" section. In the dropdown menu called "Code entry type", select "Upload a .zip file", and upload the file you've just zipped;
- Now, find the "Environment variables" section and set "PUBLISH_KEY" to your PubNub publish key, and "SUBSCRIBE_KEY" to your PubNub subscribe key;
- Your Lambda function is ready!

### AWS IoT button
- Download the AWS IoT app on your mobile device (iOS/Android) and open it;
- Setup the button on your Wi-Fi, and when it asks what you'd like to trigger when the button is pressed, select the Lambda function you just created.

### Building the app
- Open the Android project in Android Studio and compile it;
- Install the app on your Android phone.

## Usage
- Open the app on your Android phone and grant it all the permissions it requests;
- Go into the settings and click "Setup triggers". This will allow you to link IoT buttons with your phone;
- Add a trigger by typing in the serial number of your IoT button;
- Now, go back into settings and enable "Background audio recording";
- Congratulations, you're now ready to record some audio!