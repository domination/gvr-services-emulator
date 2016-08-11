# How to use

## Preparation

Download and install [controller_emulator.apk](https://github.com/googlevr/gvr-android-sdk/tree/6a1632af2a08619e9a27c75acec4650b81acb6c7/ndk-beta/apks/controller_emulator.apk?raw=true) on your _controller phone_.

On your _headset phone_ with Android version bewteen 4.4 and 6.0.1 download and install **services-gvr-emulator.apk** ([direct link](https://github.com/domination/gvr-services-emulator/blob/master/apks/services-gvr-emulator.apk?raw=true))

Download and install **samples-controllerclient.apk** ([direct link](https://github.com/domination/gvr-services-emulator/blob/master/apks/samples-controllerclient.apk?raw=true) - version prepared for Android >= KitKat).

## Setup

### Wi-Fi connection

On your _controller phone_ run **Controller Emulator**  - at the top you will see ip address.

On your _headset phone_ run **Emulator VR Services** > Controller Emulator Settings > Wi-Fi - should be enabled,
below this in section "Wi-Fi settings", complete field "IP Address" with ip address of your _controller phone_.

### Bluetooth connection

Pair your _headset phone_ with _controller phone_.

On your _controller phone_ run **Controller Emulator**.

On your _headset phone_ run **Emulator VR Services** > Controller Emulator Settings > Bluetooth - should be enabled,
below this in section "Bluetooth settings", choose proper "Device address" of your _controller phone_.

## Simple check

On your _headset phone_ run **Daydream Controller Sample**, you should see "Controller Connection: CONNECTED" and on _controller phone_ you also should see "CONNECTED".

Play with you _controller phone_ and observe changes on _headset phone_

## Demo

Download [controllerpaint.apk](https://github.com/googlevr/gvr-android-sdk/tree/6a1632af2a08619e9a27c75acec4650b81acb6c7/ndk-beta/apks/controllerpaint.apk?raw=true) on your _headset phone_ and draw with your _controller phone_.
