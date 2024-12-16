> ## Want to add chat to your app?
>
> [Stream](https://getstream.io/chat/) powers chat for over 1 billion end-users. Get started with our free chat API trial.


# Stream Fastlane Actions

## Getting Started

This project is a [_fastlane_](https://github.com/fastlane/fastlane) plugin. To get started with `fastlane-plugin-stream_actions`, add it to your project by updating the `Pluginfile`:

```ruby
gem 'fastlane-plugin-stream_actions'
```

## About Stream Fastlane Actions

Stream Actions are used to share the scripts and fastlane actions across multiple repositories and automate the release process of iOS Stream SDKs.

## Start working on this plugin

First of all, install any dependencies

```bash
bundle install
```

## Run tests for this plugin

To run both the tests, and code style validation, run

```bash
bundle exec rake
```

To automatically fix many of the styling issues, use

```bash
bundle exec rubocop -a
```

## Release a new version

To release the plugin, bump the plugin version and run

```bash
bundle exec fastlane release
```

## Issues and Feedback

For any other issues and feedback about this plugin, please submit it to this repository.

## Troubleshooting

If you have trouble using plugins, check out the [Plugins Troubleshooting](https://docs.fastlane.tools/plugins/plugins-troubleshooting/) guide.

## Using _fastlane_ Plugins

For more information about how the `fastlane` plugin system works, check out the [Plugins documentation](https://docs.fastlane.tools/plugins/create-plugin/).

## About _fastlane_

_fastlane_ is the easiest way to automate beta deployments and releases for your iOS and Android apps. To learn more, check out [fastlane.tools](https://fastlane.tools).
