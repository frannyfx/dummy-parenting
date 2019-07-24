let PubNub = require("pubnub");

// Keys
const publishKey = process.env.PUBLISH_KEY;
const subscribeKey = process.env.SUBSCRIBE_KEY;

// Create PN connection
let pn = new PubNub({
    publishKey: publishKey,
    subscribeKey: subscribeKey,
    ssl: true
});

exports.handler = function (event, context) {
    console.log(`Received AWS button press from button "${event.serialNumber}" with ${event.batteryVoltage} battery remaining.`);

    // Execute
    pn.publish({
        message: { hello: true },
        channel: event.serialNumber !== undefined ? event.serialNumber : "trigger_test"
    }, (status, response) => {
        if (status.error) {
            console.error("Something went wrong while triggering!", status);
            return;
        }

        console.log("Sent trigger message successfully.");
    });
}