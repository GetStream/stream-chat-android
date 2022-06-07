Welcome to Stream’s Android repository! We welcome all feedback and we hope this document helps you contribute to our repo! 🎉.

We'll guide you through our repository structure, present our styling guidelines and describe the pull request and approval process.

These are guidelines and don't have to be followed to a letter, if you're looking to contribute, feel free to do your best and what you're comfortable with, and we'll take care of the rest! 🤗

---

# If I have a question, do I need to read this guide? 💬

Not really! This guide is focused on contributions, so if you have questions, want to report a bug or want to request a new feature, use our [Android Chat SDK Task and Bug Report](https://github.com/GetStream/stream-chat-android/issues/new/choose) templates.

We'll do our best to respond as soon as possible and provide you with guidance and the more details you can provide, the easier it is for us to understand what you need help with!

---

# What should I know before diving into code? 🤔

Our Android repository contains several modules that each represent an artifact on Maven, as described in our [documentation](https://getstream.io/chat/docs/sdk/android/basics/dependencies/). We also have a few modules related to documentation and testing, which are not published as libraries.

If you haven't already, make sure to read our [README](https://github.com/GetStream/stream-chat-android/blob/main/README.md), to learn all about the repo, our codebase, documentation and much more.

// TODO - image of the repo and the overview.

<img width="1436" alt="Screen_Shot_2021-03-31_at_4 13 52_PM" src="https://user-images.githubusercontent.com/20601437/124240912-8791a080-db1b-11eb-9467-b00e9d14b1ca.png">

### Important modules & files 🧱

- `stream-chat-android-client`
- `stream-chat-android-offline`
- `stream-chat-android-ui-common`

- `stream-chat-android-compose-sample`
- `stream-chat-android-compose`
- `stream-chat-android-ui-components-sample`
- `stream-chat-android-ui-components`

- `CHANGELOG.md` 
- `DEPRECATIONS.md`


### Current Stream Packages

`stream_chat` - Stream Chat is a low-level wrapper around Stream's REST API and web sockets. It contains minimal external dependencies and does not rely on Flutter. It is possible to use this package on most platforms supported by Dart.

`stream_chat_flutter_core` - This package provides business logic to fetch common things required to integrate Stream Chat into your application. The core package allows more customization, providing business logic but no UI components.

`stream_chat_flutter` - This library includes both a low-level chat SDK and a set of reusable and customizable UI components.

`stream_chat_persistence` - This package provides a persistence client for fetching and saving chat data locally. Stream Chat Persistence uses Moor as a disk cache.

`stream_chat_localizations` - This package provides a set of localizations for the SDK.

### Local Setup

Congratulations! 🎉.  You've successfully cloned our repo, and you are ready to make your first contribution. Before you can start making code changes, there are a few things to configure.

**Melos Setup**

Stream uses `melos` to manage our mono-repository. For those unfamiliar, Melos is used to  split up large code bases into separate independently versioned packages. To install melos, developers can run the following command:

```bash
pub global activate melos 
```

Once activated, users can now "bootstrap" their local clone by running the following:

```bash
melos bootstrap
```

Bootstrap will automatically fetch and link dependencies for all packages in the repo. It is the melos equivalent of running `flutter pub get`.

Bonus Tip: Did you know it is possible to define and run custom scripts using Melos? Our team uses custom scripts for all sorts of actions like testing, lints, and more.

To run a script, use `melos run <script name>`.

---

# How can I contribute?

Are you ready to dive into code? It's pretty easy to get up and running with your first Stream contribution. If this is your first time sending a PR to Stream, please read the above section on [local setup](https://www.notion.so/Stream-s-Contribution-Guide-e18e1d57295f4fa8836a115d3fa3d5e7) before continuing.

## Filing bugs 🐛

Before filing bugs, take a look at our existing backlog. For common bugs, there might be an existing ticket on GitHub.

To quickly narrow down the amount of tickets on Github, try filtering based on the label that best suites the bug.

![image](https://user-images.githubusercontent.com/20601437/124240983-9d9f6100-db1b-11eb-952f-3c0cc60a910e.png)

Didn't find an existing issue? Go ahead and file a new bug using one of our pre-made issue templates.

![image](https://user-images.githubusercontent.com/20601437/124241045-aee86d80-db1b-11eb-89eb-f4189019ac3e.png)

Be sure to provide as much information as possible when filing bug reports. A good issue should have steps to reproduce and information on your development environment and expected behavior.

Screenshots and gifs are always welcomed :)

## Feature Request 💡

Have an idea for a new feature? We would love to hear about it!

Our team uses GitHub discussions to triage and discuss feature requests. Before opening a new topic, please check our existing issues and pull requests to ensure the feature you are suggesting is not already in progress.

To file a feature request, select the "Discussions" tab on our GitHub repo or [visit this link](https://github.com/GetStream/stream-chat-flutter/discussions/new). Once there, change the default category to "**💡 Ideas**", then write a brief description of your feature/change.

Screenshots, sketches, and sample code are all welcomed!

![image](https://user-images.githubusercontent.com/20601437/124241092-bc055c80-db1b-11eb-9205-7e3d7c157af1.png)

Here are some common questions to answer when filing a feature request:

**Is your feature request related to a problem? Please describe.**

A clear and concise description of what the problem is. Ex. I'm always frustrated when [...]

**Describe the solution you'd like.**

A clear and concise description of what you want to happen.

**Describe alternatives you've considered.**

A clear and concise description of any alternative solutions or features you've considered.

**Additional context.**

Add any other context or screenshots about the feature request here.

## Pull Request 🎉

![image](https://user-images.githubusercontent.com/20601437/124241146-c7f11e80-db1b-11eb-9588-d9f578ec004a.png)

Thank you for taking the time to submit a patch and contribute to our codebase. You rock!

Before we can land your pull request, please don't forget to [sign Stream's CLA (Contributor License Agreement](https://docs.google.com/forms/d/e/1FAIpQLScFKsKkAJI7mhCr7K9rEIOpqIDThrWxuvxnwUq2XkHyG154vQ/viewform). 📝

### PR Semantics 🦄

Our team uses [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) when coding and creating PRs. This standard makes it easy for our team to review and identify commits in our repo quickly.

While we don't expect developers to follow the specification down to every commit message, we enforce semantics on PR titles.

PR titles should follow the format below:

```jsx
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

1. **fix:** a commit of the *type* `fix` patches a bug in your codebase (this correlates with **`[PATCH](http://semver.org/#summary)`** in Semantic Versioning).
2. **feat:** a commit of the *type* `feat` introduces a new feature to the codebase (this correlates with **`[MINOR](http://semver.org/#summary)`** in Semantic Versioning).
3. **BREAKING CHANGE:** a commit that has a footer `BREAKING CHANGE:`, or appends a `!` after the type/scope, introduces a breaking API change (correlating with **`[MAJOR](http://semver.org/#summary)`** in Semantic Versioning). A BREAKING CHANGE can be part of commits of any *type*.
4. *types* other than `fix:` and `feat:` are allowed, for example **[@commitlint/config-conventional](https://github.com/conventional-changelog/commitlint/tree/master/%40commitlint/config-conventional)** (based on the **[the Angular convention](https://github.com/angular/angular/blob/22b96b9/CONTRIBUTING.md#-commit-message-guidelines)**) recommends `build:`, `chore:`, `ci:`, `docs:`, `style:`, `refactor:`, `perf:`, `test:`, and others.
5. *footers* other than `BREAKING CHANGE: <description>` may be provided and follow a convention similar to **[git trailer format](https://git-scm.com/docs/git-interpret-trailers)**.

### Testing

At Stream, we value testing. Every PR should include passing tests for existing and new features. To run our test suite locally, you can use the following *melos* command:

```bash
> melos run test:dart
> melos run test:flutter
```

### Our Process // TODO review & update/expand

By default, our development branch is `develop`. Contributors should create new PRs based on `develop` when working on new features.

Develop is merged into **main** after the team performs various automated and QA tests on the branch. Main can be considered our stable branch — it represents the latest published release on Maven Central.

---

# Versioning Policy

All of the Stream Chat packages follow [semantic versioning (semver)](https://semver.org/).

See our [versioning policy documentation](https://getstream.io/chat/docs/sdk/flutter/basics/versioning_policy/) for more information.

// TODO - deprecations and our lifecycle

---

# Styleguides 💅 // TODO Compose guidelines

![image](https://user-images.githubusercontent.com/20601437/124241186-d17a8680-db1b-11eb-9a21-3df305674ca9.png)

We use style guides and lint checks to keep our code consistent and maintain best practices. Our team uses Dart's built-in analyzer for linting and enforcing code styles. The full list of analyzer rules can be found below.

## Lint/Detekt Rule 📖 // TODO