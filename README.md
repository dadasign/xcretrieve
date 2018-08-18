# XCRetrieve
XCRetrieve is a complete tool for SMS based GPS location sharing.
* let others find you when you can't answer the phone, they just need to text you with a defined code;
* easily share your position in in several common formats including a Google Maps link;
* view received locations on Google Maps or Open Street Maps;
* define a default contact to allow sharing or requesting their location with a single tap;
* works without internet access, no external services, just simple SMS based communication in human readable formats.

## Download
You can find it at https://play.google.com/store/apps/details?id=com.dadasign.xcretrieve

## Build
You will need to provide your own `google-services.json` at `XCRetrive/` and modify 
[XCRetrieve/src/main/AndroidManifest.template.xml](https://github.com/dadasign/xcretrieve/blob/master/XCRetrieve/src/main/AndroidManifest.template.xml)
to contain a Google Maps API key.
