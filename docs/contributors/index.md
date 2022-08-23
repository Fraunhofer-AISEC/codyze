---
title: "Contributing to Codyze"
linkTitle: "Contributing to Codyze"
no_list: true
weight: 10
description: >
  Contributing to Codyze and updating this documentation
---


Codyze is open source and an ongoing effort to support developers and auditors in improving the quality of security-critical code. 
We welcome everybody to contribute to this project, be it fixing a typo in the documentation or implementing whole new language support and analysis modules.

!!! note

    Please note that parts of this Codyze are backed by research projects. 
    If you plan to make larger contributions, please contact us upfront to align your plans with possibly ongoing projects.


## Contributing to this documentation

This documentation lives in the same repository as the code and the idea is that both are always consistent. 
If you make changes in the code that affect the documentation, please update the documentation in the same pull request.

We use [MkDocs](https://github.com/mkdocs/mkdocs/) for generating our documentation site and [Material for MkDocs](https://github.com/squidfunk/mkdocs-material) as our theme.  

To extend or fix errors in this documentation, proceed as follows:

1. Fork the [Codyze repo](https://github.com/Fraunhofer-AISEC/codyze) on GitHub.
1. Make your changes and send a pull request (PR).
1. If you're not yet ready for a review, add "WIP" to the PR name to indicate 
  it's a work in progress.
1. Continue updating your doc and pushing your changes until you're happy with 
  the content.
1. When you're ready for a review, add a comment to the PR, and remove any
  "WIP" markers.

### Previewing your changes locally

If you want to run your own local server to preview your changes as you work, you can either install MkDocs or use a docker.

For installing MkDocs, please refer to the [Material for MkDocs documentation](https://squidfunk.github.io/mkdocs-material/getting-started/).

For the docker, please refer to the [README](https://github.com/Fraunhofer-AISEC/codyze/tree/main/docs/README.md).

### Creating an issue

If you've found a problem in the docs, but you're not sure how to fix it yourself, please create an issue in the [Codyze repo](https://github.com/Fraunhofer-AISEC/codyze/issues) and add the label `documentation` to it. You can also create an issue about a specific page by clicking the **Create Issue** button in the top right hand corner of the page.

## Contributing to Codyze

### Opening an issue

We welcome any bug reports! Please use the issue tracker only to report bugs or unexpected behavior. If you have general questions about the project or need help in setting it up, please reach out to us directly and **do not** the issue tracker.

To report a bug, make sure to include the following information:

1. What have you done? Provide the minimal set of files needed to reproduce the bug. If we cannot reproduce it, it's not a bug.
1. What outcome have you expected, what outcome did you get?
1. Can you provide any hints on what might be the cause of the bug and how you think it should be fixed?


### Useful resources

* [Material for MkDocs user guide](https://squidfunk.github.io/mkdocs-material/): All about Material, the documentation template we use, including how it manages navigation, look and feel, and multi-language support.
* [MkDocs user guide](https://www.mkdocs.org/): Comprehensive reference for MkDocs.
* [Github Hello World!](https://guides.github.com/activities/hello-world/): A basic introduction to GitHub concepts and workflow.


