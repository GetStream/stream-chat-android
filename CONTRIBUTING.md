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

![Android Repository Structure](https://user-images.githubusercontent.com/17215808/173359688-31ec7680-0a59-4ad6-91a4-f538a935ce26.PNG)

### Important modules & files 🧱

- `stream-chat-android-client`: Holds all the code connected to the **low-level-client**, used to request data from and send information to the API.
- `stream-chat-android-offline`: As an addition to the **client** module, you can use **offline** to introduce persistence and caching to your app.
- `stream-chat-android-ui-common`: Common UI-related code and utility functions.

- `stream-chat-android-compose-sample`: The main sample app featuring the Compose SDK. 
- `stream-chat-android-compose`: Our Compose SDK that features channels and messages components, as well as other utilities.
- `stream-chat-android-ui-components-sample`: The main sample app featuring the XML (UI Components) SDK. 
- `stream-chat-android-ui-components`: Our XML (UI Components) SDK that features channels and messages components, as well as other utilities.

- `CHANGELOG.md` : List of changes made in each version of the SDK.
- `DEPRECATIONS.md`: List of deprecated code in our SDK and its deprecation process timeline.

These provide corresponding artifacts on [Maven Central](https://search.maven.org/search?q=g:io.getstream) that you can plug into your projects. There are several other modules and files available in the repository and we've outlined them in our [README](https://github.com/GetStream/stream-chat-android), so be sure to check them out.

### Local Setup

Congratulations! 🎉.  You've successfully cloned our repo, and you are ready to make your first contribution. Our setup is super simple, so you don't have to do any additional work.

As long as you have the latest stable [Android Studio](https://developer.android.com/studio) version, you should be fine. :]

---

# How can I contribute?

Are you ready to dive into code? It's pretty easy to get up and running with your first Stream contribution. Although, you don't have to write code to contribute to our repository. There are a few ways to help:

* Opening issues/bug reports.
* Proposing feature requests and SDK improvements.
* Creating pull requests to improve our codebase.

Let's go over the process for each of these items!

## Opening issues & bug reports 🐛

Using the GitHub repository and project management system, you can open issues in any public repo. Before filing bugs, take a look at our existing backlog. For common items or reports, there might be an existing ticket on GitHub.

To quickly narrow down the number of tickets on Github, try filtering based on the label that best suites the bug or a part of our SDK.

![Android Project Labels](https://user-images.githubusercontent.com/17215808/173360221-c698ff11-b104-4766-b12f-646e86de1fa8.PNG)

Some of the useful labels to keep track of are:

* **core**: These items are connected to the **low-level-client** and our business logic and/or persistence layers.
* **ui-components**: Items that relate to the XML (UI Components) SDK.
* **compose**: Items that relate to the Compose SDK.
* **feature-request**: Any request that can be considered a new feature, or an improvement to the existing feature set.
* **bug**: Anything that doesn't work the way it should.

Didn't find an existing issue? Go ahead and file a new bug using one of our pre-made issue templates.

![GitHub Issue Templates](https://user-images.githubusercontent.com/17215808/173360811-271da6f7-ad41-497e-ad15-7ff591cc01b6.PNG)

The most common template you'll use is the **Android Chat SDK Task**. It's general-purpose and can be used for bug reports, issues or feature requests.

Be sure to provide as much information as possible when filing bug reports. A good issue should have steps to reproduce and information on your development environment and expected behavior.

Screenshots, gifs and videos that outline the issues or wrong behavior are always welcome! :]

## Feature Request 💡

Have an idea for a new feature? We would love to hear about it!

We have GitHub discussions to discuss feature requests, but you can also open an issue. If it's something we can support and goes in the same direction as our product, we'll look into it! Before opening a new topic, please check our existing issues and pull requests to ensure the feature you are suggesting is not already in progress.

To file a feature request, select the "Discussions" tab on our GitHub repo or [visit this link](https://github.com/GetStream/stream-chat-android/discussions/new). Once there, change the default category to "**💡 Ideas**", then write a brief description of your feature/change.

Screenshots, sketches, and sample code are all welcome!

![Starting a discussion.](https://user-images.githubusercontent.com/17215808/173364463-2b26ecc6-ce92-424f-8e14-54d332d0d110.PNG)

Here are some common questions to answer when filing a feature request:

**Is your feature request related to a problem? Please describe.**

A clear and concise description of what the problem is. E.g. For my use case, I'm trying to solve [...]

**Describe the solution you'd like.**

A clear and concise description of what you want to happen.

**Describe alternatives you've considered.**

A clear and concise description of any alternative solutions or features you've considered.

**Additional context.**

Add any other context or screenshots about the feature request here.

## Pull Request 🎉

![image](https://user-images.githubusercontent.com/20601437/124241146-c7f11e80-db1b-11eb-9588-d9f578ec004a.png)

Thank you for taking the time to submit a patch and contribute to our codebase. You rock!

If this is your first time opening a PR for a Stream repo, please read our [pull request template](https://github.com/GetStream/stream-chat-android/blob/main/.github/pull_request_template.md). It contains valuable information about each part of our PR process. 

It's also important to note that **before we merge** your pull request, please don't forget to [sign Stream's CLA (Contributor License Agreement](https://docs.google.com/forms/d/e/1FAIpQLScFKsKkAJI7mhCr7K9rEIOpqIDThrWxuvxnwUq2XkHyG154vQ/viewform). 📝

### PR Semantics 🦄 TODOODODODOOD -> leftover

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