Welcome to Stream’s Android repository! We welcome all feedback and we hope this document helps you contribute to our repo! 🎉.

We'll guide you through our repository structure, present our styling guidelines and describe the pull request and approval process.

These are guidelines and don't have to be followed to a letter, if you're looking to contribute, feel free to do your best and what you're comfortable with, and we'll take care of the rest! 🤗

---

# If I have a question, do I need to read this guide? 💬

Not really! This guide is focused on contributions, so if you have questions, want to report a bug or want to request a new feature, there are two ways you can raise the questions:
- Look into [our Discussions]() and see if someone had already asked the same question and if there's been any reasoning why we support or don't support a specific feature.
- Use our [Android Chat SDK Task and Bug Report](https://github.com/GetStream/stream-chat-android/issues/new/choose) templates to open an issue.

> **Note**: Sometimes there are duplicate issues or a question has been answered in discussions, so we recommend looking into both first and doing a bit of research, rather than immediately opening tasks.

We'll do our best to respond as soon as possible and provide you with guidance and the more details you can provide, the easier it is for us to understand what you need help with!

---

# What should I know before diving into code? 🤔

Our Android repository contains several modules that each represent an artifact on Maven, as described in our [documentation](https://getstream.io/chat/docs/sdk/android/basics/dependencies/). We also have a few modules related to documentation and testing, which are not published as libraries.

If you haven't already, make sure to read our [README](https://github.com/GetStream/stream-chat-android/blob/main/README.md), to learn all about the repo, our codebase, documentation and much more.

![Android Repository Structure](https://user-images.githubusercontent.com/17215808/173359688-31ec7680-0a59-4ad6-91a4-f538a935ce26.PNG)

### Important modules & files 🧱

- `stream-chat-android-client`: Holds all the code connected to the **low-level-client**, used to request data from and send information to the API.
- `stream-chat-android-ui-common`: Common UI-related code and utility functions.

- `stream-chat-android-compose-sample`: The main sample app featuring the Compose SDK.
- `stream-chat-android-compose`: Our Compose SDK that features channels and messages components, as well as other utilities.
- `stream-chat-android-ui-components-sample`: The main sample app featuring the XML (UI Components) SDK.
- `stream-chat-android-ui-components`: Our XML (UI Components) SDK that features channels and messages components, as well as other utilities.

- `CHANGELOG.md` : List of changes made in each version of the SDK.
- `DEPRECATIONS.md`: List of deprecated code in our SDK and its deprecation process timeline.

These provide corresponding artifacts on [Maven Central](https://search.maven.org/search?q=g:io.getstream) that you can plug into your projects. There are several other modules and files available in the repository and we've outlined them in our [README](https://github.com/GetStream/stream-chat-android/blob/main/README.md), so be sure to check them out.

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

Additionally, as we're fully open source, we have two main projects that we use for opening Issues, planning Milestones and organizing work for each quarter:

* [Android UI Team Planning](https://github.com/orgs/GetStream/projects/6/views/1): Project for any work **within the UI scope** of the SDK.
  * This includes providing new user-facing functionality, new UI components, improving the design, exposing more customization, building new features that the user can interface with and more.
* [Android Core Team Planning](https://github.com/orgs/GetStream/projects/7/views/1): Project for any work **within the Core scope** of the SDK.
  * This includes any low-level-client features and functionality, as well as improvements to our persistence and networking layers.

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

### PR Semantics 🦄

Our team uses simple, descriptive commits when coding and creating PRs. We try to make as many smaller commits as possible, to keep the history clean. This standard makes it easy for our team to review and identify commits in our repo quickly.

While we don't expect developers to follow the specification down to every commit message, we enforce semantics on the PR structure.

PR titles should follow the format: `<issue-number> - <PR-short-description>`. Furthermore, we have a very detailed structure in our PRs that helps us maintain quality of our SDK. It also helps us clearly communicate changes within each PR, that's outlined in the [PR template](https://github.com/GetStream/stream-chat-android/blob/main/.github/pull_request_template.md).

* **Goal**: This describes what we're trying to achieve in the PR. Use this section to give more context and also [link to a GitHub issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) if possible.
* **Implementation details**: Used to give more technical context and description about the changes. Use this section to explain the changes in detail and why you decided on that approach.
* **UI Changes**: This section is used to add relevant before/after images and/or videos for the change. Only useful if the change has any UI impact, like design changes, or visual behavior fixes, e.g. loading of items.
* **Testing**: You can explain the steps to test the PR here. Include as much information and as clear steps to reproduce previous and new behavior and/or UI. We also provide git patches which can be used to trigger new behavior or bugs, if it's not easily reproducible otherwise.
* **Contributor Checklist**: This part is for you, the contributor, to check off as you create a PR. It has two sections.
  * **General**: Make sure that all the items are checked off and that the PR is created, assigned and people are notified appropriately.
  * **Code & documentation**: Make sure that all required documentation is up to date, that the code is tested and the changes are easily comparable in the PR.
* **Reviewer Checklist**: This section is for reviewers. You typically don't check anything off here, only remove items which don't make sense for the PR. E.g. remove "Bugs validated" if there are no bugfixes in the PR.

If you're looking for a great example of a PR with all the sections filled correctly, check out our [PR on Compose Video thumbnails](https://github.com/GetStream/stream-chat-android/pull/4096).

### Testing

At Stream, we value testing. Every PR should include passing tests for existing and new features. To run our test suite locally, you can use the Gradle tasks. We also run several other layers of testing:
* CI/CD Unit tests suite.
* CI/CD UI Tests suite.
* CI/CD Snapshot tests suite.

All these are used to cross-compare behavior across versions of the SDK and changes, to make sure nothing is broken.

### Our Process

By default, our development branch is `develop`. Contributors should create new PRs based on `develop` when working on new features.

Develop is merged into **main** after the team performs various automated and QA tests on the branch. Main can be considered our stable branch — it represents the latest published release on Maven Central.

Additionally, we might have another branch for older major versions of the SDK, such as v3, v4, v5 and so on. **We only keep up to two major versions at any time**, one which is currently being worked on, and the previous version which we only update with the most important bugfixes or critical issues.

Whenever you can, **we recommend migrating to the latest major version**, as it has running releases in a stable cadence of ~2 weeks, with patch versions in between, if required.


---

# Versioning Policy

The Android SDK doesn't follow strict semantic versioning. Our versioning and breaking/deprecation policies are [well documented](https://getstream.io/chat/docs/sdk/android/basics/dependencies/#versioning) and follow common Android principles.

We try to avoid breaking changes whenever possible. We heavily rely on [Deprecated annotations](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-deprecated/) to slowly deprecate broken behavior or things we improve, in a non-breaking way.

For most things, you won't have to migrate immediately, only after a 2-4 weeks' notice about the functionality being changed or removed. Note that this time period depends on the scope of the feature and severity of the change.

In the meantime, if you want to test our latest changes, without waiting for the next release, or to prepare for the next major version, you can use our [snapshot builds](https://getstream.io/chat/docs/sdk/android/basics/dependencies/#snapshot-builds).

These allow you to follow everything developers have merged to **develop** and to test it against your SDK use case.

---

# Styleguides 💅

![image](https://user-images.githubusercontent.com/20601437/124241186-d17a8680-db1b-11eb-9a21-3df305674ca9.png)

> (Please don't use the first example)

We use style guides and lint checks to keep our code consistent and maintain best practices. We have a couple of layers of checks for both code structure, clarity and complexity:
* **Lint + Spotless**: We use Spotless in pair with Android and Kotlin Lint options to make sure our codebase is clean and nicely formatted. These checks run with every commit and fix any issues you might've caused. You can also run these checks manually, using `./gradlew spotlessApply`.
* **Detekt**: To maintain our codebase in terms of simplicity, conciseness, optimizations and more, we use Detekt. It points out various issues, such as magic numbers, overly complex functions, long pieces of code and more. It also runs with each commit, but you can run it yourself, using `./gradlew detekt` or `./gradlew detektBaseline` if you cannot fix issues Detekt lists out immediately.

Bear in mind that if these tools are not satisfied, the CI/CD actions will fail and the PR won't be cleared for merge until every check passes.