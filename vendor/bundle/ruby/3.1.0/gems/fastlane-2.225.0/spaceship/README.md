<h3 align="center">
  <a href="https://docs.fastlane.tools">
    <img src="../fastlane/assets/fastlane.png" width="100" />
    <br />
    fastlane
  </a>
</h3>

-------

<p align="center">
  <img src="assets/spaceship.png" width="470">
</p>

-------

[![Twitter: @FastlaneTools](https://img.shields.io/badge/contact-@FastlaneTools-blue.svg?style=flat)](https://twitter.com/FastlaneTools)
[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/fastlane/fastlane/blob/master/LICENSE)

_spaceship_ exposes both the Apple Developer Center and the App Store Connect API. It’s super fast, well tested and supports all of the operations you can do via the browser. It powers parts of _fastlane_, and can be leveraged for more advanced _fastlane_ features. Scripting your Developer Center workflow has never been easier!

Get in contact with the creators on Twitter: [@FastlaneTools](https://twitter.com/fastlanetools)

-------

<p align="center">
    <a href="#whats-spaceship">Why?</a> &bull;
    <a href="#usage">Usage</a> &bull;
    <a href="#installation">Installation</a> &bull;
    <a href="#technical-details">Technical Details</a> &bull;
    <a href="#need-help">Need help?</a>
</p>

-------

<h5 align="center"><em>spaceship</em> is part of <a href="https://fastlane.tools">fastlane</a>: The easiest way to automate beta deployments and releases for your iOS and Android apps.</h5>

# What's spaceship?

_spaceship_ uses a combination of [5 different API endpoints](#api-endpoints), used by the Apple Developer Portal and Xcode. As no API offers everything we need, spaceship combines all APIs for you. [More details about the APIs](#technical-details).

- Blazing fast communication using only a HTTP client
- Object oriented access to all resources
- Resistant against front-end design changes of the of the Apple Developer Portal
- One central tool for the communication
- Automatic re-trying of requests in case a timeout occurs
- No web scraping
- 90%+ test coverage by stubbing server responses

More details about why spaceship is useful on [spaceship.airforce](https://spaceship.airforce).

> No matter how many apps or certificates you have, spaceship **can** handle your scale.

## Example spaceship code

```ruby
Spaceship.login

# Create a new app
app = Spaceship.app.create!(bundle_id: "com.krausefx.app", name: "Spaceship App")

# Use an existing certificate
cert = Spaceship.certificate.production.all.first

# Create a new provisioning profile
profile = Spaceship.provisioning_profile.app_store.create!(bundle_id: app.bundle_id,
                                                         certificate: cert)

# Print the name and download the new profile
puts("Created Profile " + profile.name)
profile.download
```

## Speed

Before _spaceship_, the [fastlane tools](https://fastlane.tools) used web scraping to interact with Apple's web services. With spaceship it is possible to directly access the underlying APIs using a simple HTTP client only.

Using spaceship, the execution time of [_sigh_](https://docs.fastlane.tools/actions/sigh/) was reduced from over 1 minute to less than 5 seconds.

![assets/SpaceshipRecording.gif](assets/SpaceshipRecording.gif)

# Installation

_spaceship_ is part of _fastlane_:

    gem install fastlane

# Usage

## Playground

To try _spaceship_, just run `fastlane spaceship`. It will automatically start the `spaceship playground`. It makes it super easy to try _spaceship_ :rocket:

![assets/docs/Playground.png](assets/docs/Playground.png)

This requires you to install `pry` using `gem install pry`. `pry` is not installed by default, as most [_fastlane_](https://fastlane.tools) users won't need the `spaceship playground`. You can add the `pry` dependency to your `Gemfile`.

## Apple Developer Portal API

Open [DeveloperPortal.md](docs/DeveloperPortal.md) for code samples

## App Store Connect API

Open [AppStoreConnect.md](docs/AppStoreConnect.md) for code samples

## 2 Step Verification

When your Apple account has 2 factor verification enabled, you'll automatically be asked to verify your identity. If you have a trusted device configured for your account, then a code will appear on the device. If you don't have any devices configured, but have trusted a phone number, then a code will be sent to your phone. The resulting session will be stored in `~/.fastlane/spaceship/[email]/cookie`. The session should be valid for about one month, however there is no way to test this without actually waiting for over a month.

### Support for CI machines

#### Web sessions

See [Best Practices > Continuous Integration > Authenticating with Apple services > Method 2: Two-step or two-factor authentication > Storing a manually verified session using spaceauth](https://docs.fastlane.tools/best-practices/continuous-integration/#storing-a-manually-verified-session-using-spaceauth)

#### Transporter

See [Best Practices > Continuous Integration > Authenticating with Apple services > Method 3: Application-specific passwords](https://docs.fastlane.tools/best-practices/continuous-integration/#method-3-application-specific-passwords)

## _spaceship_ in use

All [fastlane tools](https://fastlane.tools) that communicate with Apple's web services in some way, use _spaceship_ to do so.

# Technical Details

## API Endpoints

Overview of the used API endpoints

- `https://idmsa.apple.com`:
  - Used to authenticate to get a valid session
- `https://developerservices2.apple.com`:
  - Get a list of all available provisioning profiles
  - Register new devices
- `https://developer.apple.com`:
  - List all devices, certificates, apps and app groups
  - Create new certificates, provisioning profiles and apps
  - Disable/enable services on apps and assign them to app groups
  - Delete certificates and apps
  - Repair provisioning profiles
  - Download provisioning profiles
  - Team selection
- `https://appstoreconnect.apple.com`:
  - Managing apps
  - Managing beta testers
  - Submitting updates to review
  - Managing app metadata
- `https://du-itc.appstoreconnect.apple.com`:
  - Upload icons, screenshots, trailers ...
- `https://is[1-9]-ssl.mzstatic.com`:
  - Download app screenshots from App Store Connect

_spaceship_ uses all those API points to offer this seamless experience.

## Magic involved

_spaceship_ does a lot of magic to get everything working so neatly:

- **Sensible Defaults**: You only have to provide the mandatory information (e.g. new provisioning profiles contain all devices by default)
- **Local Validation**: When pushing changes back to the Apple Developer Portal _spaceship_ will make sure only valid data is sent to Apple (e.g. automatic repairing of provisioning profiles)
- **Various request/response types**: When working with the different API endpoints, _spaceship_ has to deal with `JSON`, `XML`, `txt`, `plist` and sometimes even `HTML` responses and requests.
- **Automatic Pagination**: Even if you have thousands of apps, profiles or certificates, _spaceship_ **can** handle your scale. It was heavily tested by first using _spaceship_ to create hundreds of profiles and then accessing them using _spaceship_.
- **Session, Cookie and CSRF token**: All the security aspects are handled by _spaceship_.
- **Profile Magic**: Create and upload code signing requests, all managed by _spaceship_
- **Multiple Spaceship**: You can launch multiple _spaceships_ with different Apple accounts to do things like syncing the registered devices.

# Code of Conduct
Help us keep _fastlane_ open and inclusive. Please read and follow our [Code of Conduct](https://github.com/fastlane/fastlane/blob/master/CODE_OF_CONDUCT.md).

# License
This project is licensed under the terms of the MIT license. See the LICENSE file.

> This project and all fastlane tools are in no way affiliated with Apple Inc. This project is open source under the MIT license, which means you have full access to the source code and can modify it to fit your own needs. All fastlane tools run on your own computer or server, so your credentials or other sensitive information will never leave your own computer. You are responsible for how you use fastlane tools.
